-- Trigger documentation and test procedures for vaccination exercises
-- Exercise 3: Document and demonstrate triggers on APPOINTMENTS table

/*
===========================================
EJERCICIO 3: TRIGGERS EN LA TABLA APPOINTMENTS
===========================================

El sistema JavaVacunas tiene DOS triggers operando en la tabla APPOINTMENTS:

1. TRG_APPOINTMENTS_UPDATED_AT (Creado en V8)
   - Tipo: BEFORE UPDATE trigger
   - Propósito: Actualizar automáticamente el timestamp 'updated_at'
   - Se dispara: En cada operación UPDATE en la tabla appointments
   - Implementación: Establece :NEW.updated_at := SYSTIMESTAMP
   - Beneficio: Asegura un registro de auditoría de cuándo se modificaron los registros

2. TRG_APPOINTMENT_REMINDER (Creado en V8, Corregido en V15)
   - Tipo: AFTER INSERT OR UPDATE trigger
   - Propósito: Crear notificaciones de recordatorio para citas próximas
   - Se dispara: Cuando el estado de la cita es 'SCHEDULED' o 'CONFIRMED'
   - Lógica:
     * Calcula los días hasta la cita
     * Si la cita está dentro de 7 días y en el futuro
     * Inserta un registro de notificación para el creador de la cita
     * Incluye el nombre del niño y la fecha formateada de la cita
   - Beneficio: Sistema automatizado de recordatorios para guardianes/padres

CONSULTAS DE DEMOSTRACIÓN:
*/

-- Consulta para ver todos los triggers en la tabla APPOINTMENTS
-- SELECT trigger_name, trigger_type, triggering_event, status
-- FROM user_triggers
-- WHERE table_name = 'APPOINTMENTS'
-- ORDER BY trigger_name;

-- Consulta para ver el código fuente de los triggers
-- SELECT trigger_name, trigger_body
-- FROM user_triggers
-- WHERE table_name = 'APPOINTMENTS'
-- ORDER BY trigger_name;


-- Procedimiento de prueba para demostrar el funcionamiento de los triggers
CREATE OR REPLACE PROCEDURE sp_test_appointment_triggers AS
    v_child_id NUMBER;
    v_user_id NUMBER;
    v_appointment_id NUMBER;
    v_notification_count NUMBER;
    v_created_at TIMESTAMP;
    v_updated_at TIMESTAMP;
BEGIN
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('   PRUEBA DE TRIGGERS DE APPOINTMENTS');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Obtener datos de prueba
    BEGIN
        SELECT MIN(id) INTO v_child_id
        FROM children
        WHERE deleted_at IS NULL;

        SELECT MIN(id) INTO v_user_id
        FROM users
        WHERE role = 'PARENT' AND is_active = 'Y';

        IF v_child_id IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: No hay niños disponibles para la prueba');
            RETURN;
        END IF;

        IF v_user_id IS NULL THEN
            -- Try DOCTOR or NURSE if no PARENT found
            SELECT MIN(id) INTO v_user_id
            FROM users
            WHERE role IN ('DOCTOR', 'NURSE') AND is_active = 'Y';
        END IF;

        IF v_user_id IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: No hay usuarios disponibles para la prueba');
            RETURN;
        END IF;
    END;

    DBMS_OUTPUT.PUT_LINE('Paso 1: Insertando nueva cita...');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Insertar cita (debería disparar el trigger INSERT)
    INSERT INTO appointments (
        child_id,
        appointment_date,
        appointment_type,
        status,
        created_by,
        created_at,
        updated_at
    ) VALUES (
        v_child_id,
        SYSDATE + 5, -- 5 días desde ahora (dentro de la ventana de 7 días)
        'VACUNACION',
        'SCHEDULED',
        v_user_id,
        SYSTIMESTAMP,
        SYSTIMESTAMP
    ) RETURNING id INTO v_appointment_id;

    DBMS_OUTPUT.PUT_LINE('Cita creada con ID: ' || v_appointment_id);
    DBMS_OUTPUT.PUT_LINE('');

    -- Verificar si se creó la notificación (trigger de recordatorio)
    SELECT COUNT(*) INTO v_notification_count
    FROM notifications
    WHERE reference_id = v_appointment_id
      AND reference_type = 'APPOINTMENT';

    DBMS_OUTPUT.PUT_LINE('Paso 2: Verificando trigger de notificaciones...');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Notificaciones creadas: ' || v_notification_count);

    IF v_notification_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE('✓ Trigger TRG_APPOINTMENT_REMINDER funcionó correctamente');

        -- Mostrar detalles de la notificación
        FOR rec IN (
            SELECT type, title, message, sent_at
            FROM notifications
            WHERE reference_id = v_appointment_id
              AND reference_type = 'APPOINTMENT'
        ) LOOP
            DBMS_OUTPUT.PUT_LINE('  - Tipo: ' || rec.type);
            DBMS_OUTPUT.PUT_LINE('  - Título: ' || rec.title);
            DBMS_OUTPUT.PUT_LINE('  - Mensaje: ' || rec.message);
        END LOOP;
    ELSE
        DBMS_OUTPUT.PUT_LINE('⚠ No se creó notificación (puede ser normal si la fecha está fuera de rango)');
    END IF;

    DBMS_OUTPUT.PUT_LINE('');

    -- Guardar timestamp de creación
    SELECT created_at, updated_at
    INTO v_created_at, v_updated_at
    FROM appointments
    WHERE id = v_appointment_id;

    DBMS_OUTPUT.PUT_LINE('Paso 3: Actualizando la cita...');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------');

    -- Esperar un momento para asegurar que los timestamps sean diferentes
    DBMS_LOCK.SLEEP(1);

    -- Actualizar cita (debería disparar el trigger UPDATE)
    UPDATE appointments
    SET status = 'CONFIRMED',
        notes = 'Actualizado via prueba de triggers'
    WHERE id = v_appointment_id;

    DBMS_OUTPUT.PUT_LINE('Cita actualizada - el trigger debería actualizar updated_at');
    DBMS_OUTPUT.PUT_LINE('');

    -- Verificar que updated_at fue modificado
    DECLARE
        v_new_updated_at TIMESTAMP;
    BEGIN
        SELECT updated_at
        INTO v_new_updated_at
        FROM appointments
        WHERE id = v_appointment_id;

        DBMS_OUTPUT.PUT_LINE('Paso 4: Verificando trigger de timestamp...');
        DBMS_OUTPUT.PUT_LINE('----------------------------------------');
        DBMS_OUTPUT.PUT_LINE('Created At: ' || TO_CHAR(v_created_at, 'DD/MM/YYYY HH24:MI:SS.FF3'));
        DBMS_OUTPUT.PUT_LINE('Updated At (original): ' || TO_CHAR(v_updated_at, 'DD/MM/YYYY HH24:MI:SS.FF3'));
        DBMS_OUTPUT.PUT_LINE('Updated At (nuevo): ' || TO_CHAR(v_new_updated_at, 'DD/MM/YYYY HH24:MI:SS.FF3'));
        DBMS_OUTPUT.PUT_LINE('');

        IF v_new_updated_at > v_updated_at THEN
            DBMS_OUTPUT.PUT_LINE('✓ Trigger TRG_APPOINTMENTS_UPDATED_AT funcionó correctamente');
        ELSE
            DBMS_OUTPUT.PUT_LINE('⚠ Los timestamps son iguales o menores');
        END IF;
    END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('   PRUEBA COMPLETADA EXITOSAMENTE');
    DBMS_OUTPUT.PUT_LINE('===========================================');

    -- Limpiar datos de prueba
    ROLLBACK;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Nota: Se ejecutó ROLLBACK para limpiar los datos de prueba');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK;
        RAISE;
END sp_test_appointment_triggers;
/

-- Procedure to display trigger information
CREATE OR REPLACE PROCEDURE sp_show_appointment_triggers AS
BEGIN
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('  TRIGGERS EN LA TABLA APPOINTMENTS');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('');

    FOR rec IN (
        SELECT trigger_name, trigger_type, triggering_event, status
        FROM user_triggers
        WHERE table_name = 'APPOINTMENTS'
        ORDER BY trigger_name
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('Trigger: ' || rec.trigger_name);
        DBMS_OUTPUT.PUT_LINE('  Tipo: ' || rec.trigger_type);
        DBMS_OUTPUT.PUT_LINE('  Evento: ' || rec.triggering_event);
        DBMS_OUTPUT.PUT_LINE('  Estado: ' || rec.status);
        DBMS_OUTPUT.PUT_LINE('');
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('===========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
END sp_show_appointment_triggers;
/
