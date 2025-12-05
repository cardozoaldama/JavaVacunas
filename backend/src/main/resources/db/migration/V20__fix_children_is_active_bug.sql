-- Fix existing PL/SQL code that references non-existent children.is_active column
-- The CHILDREN table uses deleted_at for soft deletes, not is_active
-- This migration recreates affected functions/procedures to use deleted_at IS NULL

-- Fix fn_get_vaccination_coverage function
CREATE OR REPLACE FUNCTION fn_get_vaccination_coverage(
    p_start_date IN DATE,
    p_end_date IN DATE
) RETURN NUMBER IS
    v_total_children NUMBER;
    v_vaccinated_children NUMBER;
    v_coverage_percentage NUMBER;
BEGIN
    -- Count total children in age range (0-5 years) using deleted_at instead of is_active
    SELECT COUNT(DISTINCT id)
    INTO v_total_children
    FROM children
    WHERE date_of_birth BETWEEN ADD_MONTHS(p_end_date, -60) AND p_end_date
      AND deleted_at IS NULL;  -- FIXED: was is_active = 'Y'

    IF v_total_children = 0 THEN
        RETURN 0;
    END IF;

    -- Count children who received at least one vaccine in date range
    SELECT COUNT(DISTINCT vr.child_id)
    INTO v_vaccinated_children
    FROM vaccination_records vr
    JOIN children c ON c.id = vr.child_id
    WHERE vr.administration_date BETWEEN p_start_date AND p_end_date
      AND c.date_of_birth BETWEEN ADD_MONTHS(p_end_date, -60) AND p_end_date
      AND c.deleted_at IS NULL;  -- FIXED: was is_active = 'Y'

    v_coverage_percentage := (v_vaccinated_children / v_total_children) * 100;

    RETURN ROUND(v_coverage_percentage, 2);
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_get_vaccination_coverage;
/

-- Fix sp_administer_vaccine procedure
CREATE OR REPLACE PROCEDURE sp_administer_vaccine(
    p_child_id IN NUMBER,
    p_vaccine_id IN NUMBER,
    p_administered_by IN NUMBER,
    p_batch_number IN VARCHAR2,
    p_administration_date IN DATE,
    p_administration_site IN VARCHAR2 DEFAULT NULL,
    p_notes IN VARCHAR2 DEFAULT NULL,
    p_record_id OUT NUMBER
) AS
    v_inventory_id NUMBER;
    v_available_qty NUMBER;
    v_dose_number NUMBER;
    v_child_exists NUMBER;
    v_vaccine_exists NUMBER;
    v_user_exists NUMBER;
BEGIN
    -- Validate child exists and is not soft-deleted
    SELECT COUNT(*) INTO v_child_exists
    FROM children
    WHERE id = p_child_id
      AND deleted_at IS NULL;  -- FIXED: was is_active = 'Y'

    IF v_child_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Child not found or inactive');
    END IF;

    -- Validate vaccine exists and is active
    SELECT COUNT(*) INTO v_vaccine_exists
    FROM vaccines
    WHERE id = p_vaccine_id AND is_active = 'Y';

    IF v_vaccine_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Vaccine not found or inactive');
    END IF;

    -- Validate user exists and is authorized
    SELECT COUNT(*) INTO v_user_exists
    FROM users
    WHERE id = p_administered_by
      AND is_active = 'Y'
      AND role IN ('DOCTOR', 'NURSE');

    IF v_user_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'User not authorized to administer vaccines');
    END IF;

    -- Find available inventory (FIFO - earliest expiration first)
    BEGIN
        SELECT id, quantity
        INTO v_inventory_id, v_available_qty
        FROM (
            SELECT id, quantity
            FROM vaccine_inventory
            WHERE vaccine_id = p_vaccine_id
              AND (p_batch_number IS NULL OR batch_number = p_batch_number)
              AND status = 'AVAILABLE'
              AND quantity > 0
              AND expiration_date > p_administration_date
            ORDER BY expiration_date ASC, received_date ASC
        )
        WHERE ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20004, 'No available inventory for this vaccine');
    END;

    -- Calculate dose number for this vaccine
    SELECT COALESCE(MAX(dose_number), 0) + 1
    INTO v_dose_number
    FROM vaccination_records
    WHERE child_id = p_child_id
      AND vaccine_id = p_vaccine_id;

    -- Create vaccination record
    INSERT INTO vaccination_records (
        child_id,
        vaccine_id,
        administered_by,
        administration_date,
        batch_number,
        dose_number,
        administration_site,
        notes,
        created_at,
        updated_at
    ) VALUES (
        p_child_id,
        p_vaccine_id,
        p_administered_by,
        p_administration_date,
        (SELECT batch_number FROM vaccine_inventory WHERE id = v_inventory_id),
        v_dose_number,
        p_administration_site,
        p_notes,
        SYSTIMESTAMP,
        SYSTIMESTAMP
    ) RETURNING id INTO p_record_id;

    -- Update inventory quantity
    UPDATE vaccine_inventory
    SET quantity = quantity - 1,
        updated_at = SYSTIMESTAMP
    WHERE id = v_inventory_id;

    -- Update inventory status if depleted
    UPDATE vaccine_inventory
    SET status = 'DEPLETED',
        updated_at = SYSTIMESTAMP
    WHERE id = v_inventory_id
      AND quantity = 0;

    -- Mark related appointment as completed if exists
    UPDATE appointments
    SET status = 'COMPLETED',
        updated_at = SYSTIMESTAMP
    WHERE child_id = p_child_id
      AND appointment_date <= p_administration_date
      AND status IN ('SCHEDULED', 'CONFIRMED')
      AND ROWNUM = 1;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_administer_vaccine;
/

-- Fix pkg_vaccination_management package
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
                vs.recommended_age_months AS age_months,
                CASE
                    WHEN v_age_months > (vs.recommended_age_months + 1) THEN 'Y'
                    ELSE 'N'
                END AS is_overdue
            FROM vaccination_schedules vs
            JOIN vaccines v ON v.id = vs.vaccine_id
            WHERE vs.recommended_age_months <= v_age_months
              AND v.is_active = 'Y'
              AND NOT EXISTS (
                  SELECT 1
                  FROM vaccination_records vr
                  WHERE vr.child_id = p_child_id
                    AND vr.vaccine_id = vs.vaccine_id
              )
            ORDER BY vs.recommended_age_months, v.name
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
        SELECT MIN(vs.recommended_age_months)
        INTO v_next_scheduled_age
        FROM vaccination_schedules vs
        WHERE vs.recommended_age_months > v_age_months
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
        v_child_exists NUMBER;
        v_vaccine_active VARCHAR2(1);
    BEGIN
        -- Check if child exists and is not soft-deleted
        SELECT COUNT(*) INTO v_child_exists
        FROM children
        WHERE id = p_child_id
          AND deleted_at IS NULL;  -- FIXED: was is_active = 'Y'

        IF v_child_exists = 0 THEN
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
        SELECT MIN(recommended_age_months) INTO v_min_age_months
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
            SELECT COUNT(DISTINCT recommended_age_months) INTO v_max_doses
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
        WHERE recommended_age_months <= v_age_months;

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
              WHERE recommended_age_months <= v_age_months
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
