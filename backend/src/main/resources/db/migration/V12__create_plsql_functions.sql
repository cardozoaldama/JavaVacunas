-- Create PL/SQL functions for vaccination management system

-- Function to calculate child age in months
CREATE OR REPLACE FUNCTION fn_get_child_age_months(
    p_child_id IN NUMBER
) RETURN NUMBER IS
    v_birth_date DATE;
    v_age_months NUMBER;
BEGIN
    SELECT date_of_birth INTO v_birth_date
    FROM children
    WHERE id = p_child_id;

    v_age_months := MONTHS_BETWEEN(SYSDATE, v_birth_date);

    RETURN FLOOR(v_age_months);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
    WHEN OTHERS THEN
        RAISE;
END fn_get_child_age_months;
/

-- Function to get available stock for a vaccine
CREATE OR REPLACE FUNCTION fn_get_available_stock(
    p_vaccine_id IN NUMBER
) RETURN NUMBER IS
    v_total_stock NUMBER;
BEGIN
    SELECT COALESCE(SUM(quantity), 0)
    INTO v_total_stock
    FROM vaccine_inventory
    WHERE vaccine_id = p_vaccine_id
      AND status = 'AVAILABLE'
      AND expiration_date > SYSDATE
      AND quantity > 0;

    RETURN v_total_stock;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_get_available_stock;
/

-- Function to check if a vaccine is overdue for a child
CREATE OR REPLACE FUNCTION fn_is_vaccine_overdue(
    p_child_id IN NUMBER,
    p_vaccine_id IN NUMBER
) RETURN VARCHAR2 IS
    v_birth_date DATE;
    v_age_months NUMBER;
    v_scheduled_age_months NUMBER;
    v_already_received NUMBER;
    v_grace_period_months CONSTANT NUMBER := 1; -- 1 month grace period
BEGIN
    -- Check if vaccine was already administered
    SELECT COUNT(*)
    INTO v_already_received
    FROM vaccination_records
    WHERE child_id = p_child_id
      AND vaccine_id = p_vaccine_id;

    IF v_already_received > 0 THEN
        RETURN 'N'; -- Already received, not overdue
    END IF;

    -- Get child birth date
    SELECT date_of_birth INTO v_birth_date
    FROM children
    WHERE id = p_child_id;

    v_age_months := MONTHS_BETWEEN(SYSDATE, v_birth_date);

    -- Get scheduled age for this vaccine
    SELECT age_months INTO v_scheduled_age_months
    FROM vaccination_schedules
    WHERE vaccine_id = p_vaccine_id
      AND ROWNUM = 1
    ORDER BY age_months;

    -- Check if overdue (current age > scheduled age + grace period)
    IF v_age_months > (v_scheduled_age_months + v_grace_period_months) THEN
        RETURN 'Y';
    ELSE
        RETURN 'N';
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'N';
    WHEN OTHERS THEN
        RAISE;
END fn_is_vaccine_overdue;
/

-- Function to calculate vaccination coverage percentage for a date range
CREATE OR REPLACE FUNCTION fn_get_vaccination_coverage(
    p_start_date IN DATE,
    p_end_date IN DATE
) RETURN NUMBER IS
    v_total_children NUMBER;
    v_vaccinated_children NUMBER;
    v_coverage_percentage NUMBER;
BEGIN
    -- Count total children in age range (0-5 years)
    SELECT COUNT(DISTINCT id)
    INTO v_total_children
    FROM children
    WHERE date_of_birth BETWEEN ADD_MONTHS(p_end_date, -60) AND p_end_date
      AND is_active = 'Y';

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
      AND c.is_active = 'Y';

    v_coverage_percentage := (v_vaccinated_children / v_total_children) * 100;

    RETURN ROUND(v_coverage_percentage, 2);
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_get_vaccination_coverage;
/
