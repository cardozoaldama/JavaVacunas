-- Fix the trg_appointment_reminder trigger
-- The v_days_until variable was incorrectly declared as DATE instead of NUMBER

CREATE OR REPLACE TRIGGER trg_appointment_reminder
AFTER INSERT OR UPDATE ON appointments
FOR EACH ROW
WHEN (NEW.status = 'SCHEDULED' OR NEW.status = 'CONFIRMED')
DECLARE
    v_days_until NUMBER;  -- Fixed: Changed from DATE to NUMBER
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
