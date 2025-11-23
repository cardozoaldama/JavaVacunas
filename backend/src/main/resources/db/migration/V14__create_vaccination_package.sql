-- Create PL/SQL package for vaccination management

-- Package specification (interface)
CREATE OR REPLACE PACKAGE pkg_vaccination_management AS
    -- Type definition for vaccine list
    TYPE t_vaccine_record IS RECORD (
        vaccine_id NUMBER,
        vaccine_name VARCHAR2(100),
        scheduled_age_months NUMBER,
        is_overdue VARCHAR2(1)
    );

    TYPE t_vaccine_list IS TABLE OF t_vaccine_record;

    -- Get pending vaccines for a child based on age and PAI schedule
    FUNCTION get_pending_vaccines(
        p_child_id IN NUMBER
    ) RETURN t_vaccine_list PIPELINED;

    -- Calculate next appointment date for a child
    FUNCTION calculate_next_appointment(
        p_child_id IN NUMBER
    ) RETURN DATE;

    -- Validate if a vaccine can be administered to a child
    FUNCTION validate_vaccine_application(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER
    ) RETURN VARCHAR2;

    -- Get vaccination completion percentage for a child
    FUNCTION get_vaccination_completion(
        p_child_id IN NUMBER
    ) RETURN NUMBER;

    -- Get overdue vaccines count for a child
    FUNCTION get_overdue_vaccines_count(
        p_child_id IN NUMBER
    ) RETURN NUMBER;

END pkg_vaccination_management;
/

-- Package body (implementation)
CREATE OR REPLACE PACKAGE BODY pkg_vaccination_management AS

    -- Get pending vaccines for a child based on age and PAI schedule
    FUNCTION get_pending_vaccines(
        p_child_id IN NUMBER
    ) RETURN t_vaccine_list PIPELINED IS
        v_age_months NUMBER;
        v_vaccine_rec t_vaccine_record;
    BEGIN
        -- Get child age in months
        v_age_months := fn_get_child_age_months(p_child_id);

        IF v_age_months IS NULL THEN
            RETURN;
        END IF;

        -- Find vaccines not yet administered and scheduled for current age or earlier
        FOR rec IN (
            SELECT
                vs.vaccine_id,
                v.name AS vaccine_name,
                vs.age_months,
                CASE
                    WHEN v_age_months > (vs.age_months + 1) THEN 'Y'
                    ELSE 'N'
                END AS is_overdue
            FROM vaccination_schedules vs
            JOIN vaccines v ON v.id = vs.vaccine_id
            WHERE vs.age_months <= v_age_months
              AND v.is_active = 'Y'
              AND NOT EXISTS (
                  SELECT 1
                  FROM vaccination_records vr
                  WHERE vr.child_id = p_child_id
                    AND vr.vaccine_id = vs.vaccine_id
              )
            ORDER BY vs.age_months, v.name
        ) LOOP
            v_vaccine_rec.vaccine_id := rec.vaccine_id;
            v_vaccine_rec.vaccine_name := rec.vaccine_name;
            v_vaccine_rec.scheduled_age_months := rec.age_months;
            v_vaccine_rec.is_overdue := rec.is_overdue;

            PIPE ROW(v_vaccine_rec);
        END LOOP;

        RETURN;
    END get_pending_vaccines;

    -- Calculate next appointment date for a child
    FUNCTION calculate_next_appointment(
        p_child_id IN NUMBER
    ) RETURN DATE IS
        v_birth_date DATE;
        v_age_months NUMBER;
        v_next_scheduled_age NUMBER;
        v_next_appointment_date DATE;
    BEGIN
        -- Get child information
        SELECT date_of_birth INTO v_birth_date
        FROM children
        WHERE id = p_child_id;

        v_age_months := MONTHS_BETWEEN(SYSDATE, v_birth_date);

        -- Find next scheduled vaccine age that hasn't been administered
        SELECT MIN(vs.age_months)
        INTO v_next_scheduled_age
        FROM vaccination_schedules vs
        WHERE vs.age_months > v_age_months
          AND NOT EXISTS (
              SELECT 1
              FROM vaccination_records vr
              WHERE vr.child_id = p_child_id
                AND vr.vaccine_id = vs.vaccine_id
          );

        IF v_next_scheduled_age IS NULL THEN
            RETURN NULL; -- No pending vaccines
        END IF;

        -- Calculate appointment date (birth date + scheduled age in months)
        v_next_appointment_date := ADD_MONTHS(v_birth_date, v_next_scheduled_age);

        -- If date is in the past, return current date + 7 days
        IF v_next_appointment_date < SYSDATE THEN
            v_next_appointment_date := SYSDATE + 7;
        END IF;

        RETURN v_next_appointment_date;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN NULL;
    END calculate_next_appointment;

    -- Validate if a vaccine can be administered to a child
    FUNCTION validate_vaccine_application(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER
    ) RETURN VARCHAR2 IS
        v_age_months NUMBER;
        v_min_age_months NUMBER;
        v_already_administered NUMBER;
        v_available_stock NUMBER;
        v_child_active VARCHAR2(1);
        v_vaccine_active VARCHAR2(1);
    BEGIN
        -- Check if child is active
        SELECT is_active INTO v_child_active
        FROM children
        WHERE id = p_child_id;

        IF v_child_active = 'N' THEN
            RETURN 'ERROR: Child is inactive';
        END IF;

        -- Check if vaccine is active
        SELECT is_active INTO v_vaccine_active
        FROM vaccines
        WHERE id = p_vaccine_id;

        IF v_vaccine_active = 'N' THEN
            RETURN 'ERROR: Vaccine is inactive';
        END IF;

        -- Get child age
        v_age_months := fn_get_child_age_months(p_child_id);

        -- Get minimum age for this vaccine
        SELECT MIN(age_months) INTO v_min_age_months
        FROM vaccination_schedules
        WHERE vaccine_id = p_vaccine_id;

        IF v_min_age_months IS NULL THEN
            RETURN 'ERROR: Vaccine not in schedule';
        END IF;

        -- Check if child is old enough
        IF v_age_months < v_min_age_months THEN
            RETURN 'ERROR: Child too young for this vaccine';
        END IF;

        -- Check if already administered
        SELECT COUNT(*) INTO v_already_administered
        FROM vaccination_records
        WHERE child_id = p_child_id
          AND vaccine_id = p_vaccine_id;

        -- Get max doses for this vaccine (from schedule)
        DECLARE
            v_max_doses NUMBER;
        BEGIN
            SELECT COUNT(DISTINCT age_months) INTO v_max_doses
            FROM vaccination_schedules
            WHERE vaccine_id = p_vaccine_id;

            IF v_already_administered >= v_max_doses THEN
                RETURN 'ERROR: All doses already administered';
            END IF;
        END;

        -- Check inventory availability
        v_available_stock := fn_get_available_stock(p_vaccine_id);

        IF v_available_stock = 0 THEN
            RETURN 'ERROR: No stock available';
        END IF;

        -- All validations passed
        RETURN 'OK';

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 'ERROR: Child or vaccine not found';
        WHEN OTHERS THEN
            RETURN 'ERROR: ' || SQLERRM;
    END validate_vaccine_application;

    -- Get vaccination completion percentage for a child
    FUNCTION get_vaccination_completion(
        p_child_id IN NUMBER
    ) RETURN NUMBER IS
        v_age_months NUMBER;
        v_total_vaccines NUMBER;
        v_administered_vaccines NUMBER;
        v_completion_pct NUMBER;
    BEGIN
        v_age_months := fn_get_child_age_months(p_child_id);

        IF v_age_months IS NULL THEN
            RETURN 0;
        END IF;

        -- Count total vaccines scheduled for current age
        SELECT COUNT(DISTINCT vaccine_id)
        INTO v_total_vaccines
        FROM vaccination_schedules
        WHERE age_months <= v_age_months;

        IF v_total_vaccines = 0 THEN
            RETURN 100; -- No vaccines scheduled yet
        END IF;

        -- Count administered vaccines
        SELECT COUNT(DISTINCT vaccine_id)
        INTO v_administered_vaccines
        FROM vaccination_records
        WHERE child_id = p_child_id
          AND vaccine_id IN (
              SELECT vaccine_id
              FROM vaccination_schedules
              WHERE age_months <= v_age_months
          );

        v_completion_pct := (v_administered_vaccines / v_total_vaccines) * 100;

        RETURN ROUND(v_completion_pct, 2);

    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END get_vaccination_completion;

    -- Get overdue vaccines count for a child
    FUNCTION get_overdue_vaccines_count(
        p_child_id IN NUMBER
    ) RETURN NUMBER IS
        v_overdue_count NUMBER := 0;
    BEGIN
        SELECT COUNT(*)
        INTO v_overdue_count
        FROM (
            SELECT vaccine_id
            FROM TABLE(pkg_vaccination_management.get_pending_vaccines(p_child_id))
            WHERE is_overdue = 'Y'
        );

        RETURN v_overdue_count;

    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END get_overdue_vaccines_count;

END pkg_vaccination_management;
/
