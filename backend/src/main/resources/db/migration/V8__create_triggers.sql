-- Trigger to automatically update updated_at timestamp on users
CREATE OR REPLACE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger to automatically update updated_at timestamp on children
CREATE OR REPLACE TRIGGER trg_children_updated_at
BEFORE UPDATE ON children
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger to automatically update updated_at timestamp on vaccines
CREATE OR REPLACE TRIGGER trg_vaccines_updated_at
BEFORE UPDATE ON vaccines
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger to automatically update updated_at timestamp on vaccination_records
CREATE OR REPLACE TRIGGER trg_vaccination_records_updated_at
BEFORE UPDATE ON vaccination_records
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger to automatically update updated_at timestamp on appointments
CREATE OR REPLACE TRIGGER trg_appointments_updated_at
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger to automatically update updated_at timestamp on vaccine_inventory
CREATE OR REPLACE TRIGGER trg_vaccine_inventory_updated_at
BEFORE UPDATE ON vaccine_inventory
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- Trigger for audit logging on vaccination_records
CREATE OR REPLACE TRIGGER trg_audit_vaccination_records
AFTER INSERT OR UPDATE OR DELETE ON vaccination_records
FOR EACH ROW
DECLARE
    v_operation VARCHAR2(10);
    v_user_id NUMBER;
BEGIN
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_user_id := :NEW.administered_by;
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_user_id := :NEW.administered_by;
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_user_id := :OLD.administered_by;
    END IF;

    INSERT INTO audit_log (table_name, operation, record_id, user_id, timestamp)
    VALUES ('VACCINATION_RECORDS', v_operation,
            COALESCE(:NEW.id, :OLD.id), v_user_id, SYSTIMESTAMP);
EXCEPTION
    WHEN OTHERS THEN
        -- Log error but don't fail the main transaction
        NULL;
END;
/

-- Trigger to create appointment reminder notifications
CREATE OR REPLACE TRIGGER trg_appointment_reminder
AFTER INSERT OR UPDATE ON appointments
FOR EACH ROW
WHEN (NEW.status = 'SCHEDULED' OR NEW.status = 'CONFIRMED')
DECLARE
    v_days_until DATE;
    v_child_name VARCHAR2(200);
BEGIN
    v_days_until := TRUNC(:NEW.appointment_date) - TRUNC(SYSDATE);

    -- Create reminder if appointment is within 7 days
    IF v_days_until <= 7 AND v_days_until >= 0 THEN
        SELECT first_name || ' ' || last_name
        INTO v_child_name
        FROM children WHERE id = :NEW.child_id;

        -- Notify the creator (usually parent or guardian user)
        INSERT INTO notifications (
            recipient_id,
            recipient_type,
            type,
            title,
            message,
            reference_id,
            reference_type
        ) VALUES (
            :NEW.created_by,
            'USER',
            'REMINDER',
            'Recordatorio de cita',
            'Cita de vacunación para ' || v_child_name || ' el ' ||
            TO_CHAR(:NEW.appointment_date, 'DD/MM/YYYY HH24:MI'),
            :NEW.id,
            'APPOINTMENT'
        );
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

-- Trigger to alert on low inventory or expiring vaccines
CREATE OR REPLACE TRIGGER trg_inventory_alerts
AFTER INSERT OR UPDATE ON vaccine_inventory
FOR EACH ROW
DECLARE
    v_vaccine_name VARCHAR2(100);
    v_days_to_expiry NUMBER;
BEGIN
    SELECT name INTO v_vaccine_name
    FROM vaccines WHERE id = :NEW.vaccine_id;

    v_days_to_expiry := TRUNC(:NEW.expiration_date) - TRUNC(SYSDATE);

    -- Alert if quantity is low (less than 10 doses)
    IF :NEW.quantity < 10 AND :NEW.status = 'AVAILABLE' THEN
        INSERT INTO notifications (
            recipient_id,
            recipient_type,
            type,
            title,
            message,
            reference_id,
            reference_type
        )
        SELECT
            id,
            'USER',
            'WARNING',
            'Inventario bajo',
            'Stock bajo para ' || v_vaccine_name || ' (Lote: ' || :NEW.batch_number || '). Quedan ' || :NEW.quantity || ' dosis.',
            :NEW.id,
            'INVENTORY'
        FROM users
        WHERE role IN ('DOCTOR', 'NURSE') AND is_active = 'Y'
        AND ROWNUM = 1;
    END IF;

    -- Alert if vaccine is expiring soon (within 30 days)
    IF v_days_to_expiry <= 30 AND v_days_to_expiry > 0 AND :NEW.status = 'AVAILABLE' THEN
        INSERT INTO notifications (
            recipient_id,
            recipient_type,
            type,
            title,
            message,
            reference_id,
            reference_type
        )
        SELECT
            id,
            'USER',
            'ALERT',
            'Vacuna próxima a vencer',
            v_vaccine_name || ' (Lote: ' || :NEW.batch_number || ') vence en ' || v_days_to_expiry || ' días.',
            :NEW.id,
            'INVENTORY'
        FROM users
        WHERE role IN ('DOCTOR', 'NURSE') AND is_active = 'Y'
        AND ROWNUM = 1;
    END IF;

    -- Update status to EXPIRED if past expiration date
    IF :NEW.expiration_date < SYSDATE AND :NEW.status = 'AVAILABLE' THEN
        :NEW.status := 'EXPIRED';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/
