-- Create PL/SQL stored procedures for vaccination management system

-- Procedure to administer a vaccine (complete transaction)
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
    -- Validate child exists and is active
    SELECT COUNT(*) INTO v_child_exists
    FROM children
    WHERE id = p_child_id AND is_active = 'Y';

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
      AND vaccine_id = p_vaccine_id
      AND status IN ('SCHEDULED', 'CONFIRMED')
      AND appointment_date <= p_administration_date
      AND ROWNUM = 1;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_administer_vaccine;
/

-- Procedure to deduct inventory with validations
CREATE OR REPLACE PROCEDURE sp_deduct_inventory(
    p_vaccine_id IN NUMBER,
    p_quantity IN NUMBER,
    p_batch_number IN VARCHAR2 DEFAULT NULL,
    p_reason IN VARCHAR2 DEFAULT 'USAGE',
    p_deducted_by IN NUMBER
) AS
    v_remaining_qty NUMBER;
    v_inventory_id NUMBER;
    v_batch_qty NUMBER;
    v_qty_to_deduct NUMBER;

    CURSOR c_inventory IS
        SELECT id, batch_number, quantity
        FROM vaccine_inventory
        WHERE vaccine_id = p_vaccine_id
          AND (p_batch_number IS NULL OR batch_number = p_batch_number)
          AND status = 'AVAILABLE'
          AND quantity > 0
          AND expiration_date > SYSDATE
        ORDER BY expiration_date ASC, received_date ASC
        FOR UPDATE;
BEGIN
    -- Validate quantity is positive
    IF p_quantity <= 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Quantity must be positive');
    END IF;

    -- Validate user exists
    IF p_deducted_by IS NOT NULL THEN
        DECLARE
            v_user_count NUMBER;
        BEGIN
            SELECT COUNT(*) INTO v_user_count
            FROM users
            WHERE id = p_deducted_by AND is_active = 'Y';

            IF v_user_count = 0 THEN
                RAISE_APPLICATION_ERROR(-20006, 'Invalid user');
            END IF;
        END;
    END IF;

    -- Check total available stock
    v_remaining_qty := fn_get_available_stock(p_vaccine_id);

    IF v_remaining_qty < p_quantity THEN
        RAISE_APPLICATION_ERROR(-20007,
            'Insufficient stock. Available: ' || v_remaining_qty || ', Requested: ' || p_quantity);
    END IF;

    v_remaining_qty := p_quantity;

    -- Deduct from inventory batches (FIFO)
    FOR rec IN c_inventory LOOP
        EXIT WHEN v_remaining_qty = 0;

        v_batch_qty := rec.quantity;
        v_qty_to_deduct := LEAST(v_batch_qty, v_remaining_qty);

        -- Update inventory quantity
        UPDATE vaccine_inventory
        SET quantity = quantity - v_qty_to_deduct,
            updated_at = SYSTIMESTAMP
        WHERE id = rec.id;

        -- Update status if depleted
        IF v_batch_qty = v_qty_to_deduct THEN
            UPDATE vaccine_inventory
            SET status = 'DEPLETED',
                updated_at = SYSTIMESTAMP
            WHERE id = rec.id;
        END IF;

        -- Create audit log entry
        INSERT INTO audit_log (
            table_name,
            operation,
            record_id,
            user_id,
            timestamp,
            details
        ) VALUES (
            'VACCINE_INVENTORY',
            'DEDUCT',
            rec.id,
            p_deducted_by,
            SYSTIMESTAMP,
            'Deducted ' || v_qty_to_deduct || ' units. Reason: ' || p_reason
        );

        v_remaining_qty := v_remaining_qty - v_qty_to_deduct;
    END LOOP;

    IF v_remaining_qty > 0 THEN
        RAISE_APPLICATION_ERROR(-20008, 'Failed to deduct full quantity');
    END IF;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_deduct_inventory;
/
