-- Create PL/SQL package for vaccination exercises
-- Exercise 1: Package with procedure and function

-- Package specification (interface)
CREATE OR REPLACE PACKAGE pkg_vaccination_exercises AS

    -- Procedure to register a vaccine appointment
    -- Shows confirmation message via DBMS_OUTPUT in Spanish
    PROCEDURE register_vaccine(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER,
        p_appointment_date IN TIMESTAMP,
        p_created_by IN NUMBER
    );

    -- Function to check vaccine status for a child
    -- Returns 'PENDIENTE' (pending) or 'APLICADA' (applied) in Spanish
    FUNCTION ver_estado(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER
    ) RETURN VARCHAR2;

END pkg_vaccination_exercises;
/

-- Package body (implementation)
CREATE OR REPLACE PACKAGE BODY pkg_vaccination_exercises AS

    -- Procedure to register a vaccine appointment
    PROCEDURE register_vaccine(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER,
        p_appointment_date IN TIMESTAMP,
        p_created_by IN NUMBER
    ) AS
        v_appointment_id NUMBER;
        v_child_name VARCHAR2(200);
        v_vaccine_name VARCHAR2(100);
    BEGIN
        -- Validate child exists and is not soft-deleted
        BEGIN
            SELECT first_name || ' ' || last_name
            INTO v_child_name
            FROM children
            WHERE id = p_child_id
              AND deleted_at IS NULL;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('ERROR: Niño no encontrado o eliminado');
                RAISE_APPLICATION_ERROR(-20001, 'Niño no encontrado o eliminado');
        END;

        -- Validate vaccine exists and is active
        BEGIN
            SELECT name
            INTO v_vaccine_name
            FROM vaccines
            WHERE id = p_vaccine_id
              AND is_active = 'Y';
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('ERROR: Vacuna no encontrada o inactiva');
                RAISE_APPLICATION_ERROR(-20002, 'Vacuna no encontrada o inactiva');
        END;

        -- Validate user exists and is authorized
        DECLARE
            v_user_count NUMBER;
        BEGIN
            SELECT COUNT(*) INTO v_user_count
            FROM users
            WHERE id = p_created_by
              AND is_active = 'Y'
              AND role IN ('DOCTOR', 'NURSE', 'PARENT');

            IF v_user_count = 0 THEN
                DBMS_OUTPUT.PUT_LINE('ERROR: Usuario no autorizado');
                RAISE_APPLICATION_ERROR(-20003, 'Usuario no autorizado');
            END IF;
        END;

        -- Insert appointment record
        INSERT INTO appointments (
            child_id,
            appointment_date,
            appointment_type,
            status,
            scheduled_vaccines,
            created_by,
            created_at,
            updated_at
        ) VALUES (
            p_child_id,
            p_appointment_date,
            'VACUNACION',
            'SCHEDULED',
            v_vaccine_name,
            p_created_by,
            SYSTIMESTAMP,
            SYSTIMESTAMP
        ) RETURNING id INTO v_appointment_id;

        -- Display confirmation message in Spanish via DBMS_OUTPUT
        DBMS_OUTPUT.PUT_LINE('===========================================');
        DBMS_OUTPUT.PUT_LINE('   CITA REGISTRADA EXITOSAMENTE');
        DBMS_OUTPUT.PUT_LINE('===========================================');
        DBMS_OUTPUT.PUT_LINE('ID de Cita: ' || v_appointment_id);
        DBMS_OUTPUT.PUT_LINE('Niño: ' || v_child_name);
        DBMS_OUTPUT.PUT_LINE('Vacuna: ' || v_vaccine_name);
        DBMS_OUTPUT.PUT_LINE('Fecha: ' || TO_CHAR(p_appointment_date, 'DD/MM/YYYY HH24:MI'));
        DBMS_OUTPUT.PUT_LINE('Estado: SCHEDULED');
        DBMS_OUTPUT.PUT_LINE('===========================================');

        COMMIT;

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
            ROLLBACK;
            RAISE;
    END register_vaccine;

    -- Function to check vaccine status for a child
    FUNCTION ver_estado(
        p_child_id IN NUMBER,
        p_vaccine_id IN NUMBER
    ) RETURN VARCHAR2 AS
        v_count NUMBER;
    BEGIN
        -- Validate child exists and is not soft-deleted
        DECLARE
            v_child_exists NUMBER;
        BEGIN
            SELECT COUNT(*) INTO v_child_exists
            FROM children
            WHERE id = p_child_id
              AND deleted_at IS NULL;

            IF v_child_exists = 0 THEN
                RETURN 'ERROR: Niño no encontrado';
            END IF;
        END;

        -- Validate vaccine exists
        DECLARE
            v_vaccine_exists NUMBER;
        BEGIN
            SELECT COUNT(*) INTO v_vaccine_exists
            FROM vaccines
            WHERE id = p_vaccine_id;

            IF v_vaccine_exists = 0 THEN
                RETURN 'ERROR: Vacuna no encontrada';
            END IF;
        END;

        -- Check if vaccine has been administered to this child
        SELECT COUNT(*)
        INTO v_count
        FROM vaccination_records
        WHERE child_id = p_child_id
          AND vaccine_id = p_vaccine_id;

        -- Return status in Spanish
        IF v_count > 0 THEN
            RETURN 'APLICADA';
        ELSE
            RETURN 'PENDIENTE';
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            RETURN 'ERROR: ' || SQLERRM;
    END ver_estado;

END pkg_vaccination_exercises;
/
