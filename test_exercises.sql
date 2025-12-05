-- ============================================================================
-- JavaVacunas PL/SQL Exercises Test Script
-- ============================================================================
-- Este script prueba todos los ejercicios de PL/SQL creados para JavaVacunas
--
-- REQUISITOS PREVIOS:
-- 1. Ejecutar este script desde SQL*Plus conectado a la base de datos
-- 2. Asegurarse de que las migraciones V17, V18, V19, V20 se hayan aplicado
-- 3. Tener datos de prueba en las tablas (children, vaccines, users)
--
-- EJECUCIÓN:
-- sql*plus JAVACUNAS/JavaCunas123@XEPDB1 @test_exercises.sql
--
-- O desde SQL*Plus:
-- @test_exercises.sql
-- ============================================================================

-- Configuración inicial de SQL*Plus
SET SERVEROUTPUT ON SIZE 1000000;
SET LINESIZE 200;
SET PAGESIZE 100;
SET FEEDBACK OFF;
SET VERIFY OFF;

PROMPT ============================================================================
PROMPT   SUITE DE PRUEBAS - EJERCICIOS PL/SQL JAVACUNAS
PROMPT ============================================================================
PROMPT

-- Verificar que DBMS_OUTPUT está funcionando
PROMPT Verificando DBMS_OUTPUT...
BEGIN
    DBMS_OUTPUT.PUT_LINE('✓ DBMS_OUTPUT está funcionando correctamente');
END;
/

PROMPT
PROMPT ============================================================================
PROMPT   EJERCICIO 1: PKG_VACCINATION_EXERCISES
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT Test 1.1: Procedimiento REGISTER_VACCINE
PROMPT ----------------------------------------------------------------------------
DECLARE
    v_child_id NUMBER;
    v_vaccine_id NUMBER;
    v_user_id NUMBER;
BEGIN
    -- Obtener datos de prueba
    SELECT MIN(id) INTO v_child_id FROM children WHERE deleted_at IS NULL;
    SELECT MIN(id) INTO v_vaccine_id FROM vaccines WHERE is_active = 'Y';
    SELECT MIN(id) INTO v_user_id FROM users WHERE role = 'DOCTOR' AND is_active = 'Y';

    IF v_child_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay niños disponibles para la prueba');
        RETURN;
    END IF;

    IF v_vaccine_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay vacunas disponibles para la prueba');
        RETURN;
    END IF;

    IF v_user_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay usuarios disponibles para la prueba');
        RETURN;
    END IF;

    -- Llamar al procedimiento
    pkg_vaccination_exercises.register_vaccine(
        p_child_id => v_child_id,
        p_vaccine_id => v_vaccine_id,
        p_appointment_date => SYSTIMESTAMP + 7,
        p_created_by => v_user_id
    );

    ROLLBACK; -- No guardar datos de prueba

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 1.1: ' || SQLERRM);
        ROLLBACK;
END;
/

PROMPT
PROMPT ----------------------------------------------------------------------------
PROMPT Test 1.2: Función VER_ESTADO - Verificar estado de vacuna
PROMPT ----------------------------------------------------------------------------
DECLARE
    v_status VARCHAR2(50);
    v_child_id NUMBER;
    v_vaccine_id NUMBER;
    v_child_name VARCHAR2(200);
    v_vaccine_name VARCHAR2(100);
BEGIN
    -- Obtener primer niño y primera vacuna
    SELECT MIN(c.id), c.first_name || ' ' || c.last_name
    INTO v_child_id, v_child_name
    FROM children c
    WHERE c.deleted_at IS NULL;

    SELECT MIN(v.id), v.name
    INTO v_vaccine_id, v_vaccine_name
    FROM vaccines v
    WHERE v.is_active = 'Y';

    IF v_child_id IS NULL OR v_vaccine_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay datos disponibles para la prueba');
        RETURN;
    END IF;

    -- Verificar estado
    v_status := pkg_vaccination_exercises.ver_estado(v_child_id, v_vaccine_id);

    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('   VERIFICACIÓN DE ESTADO DE VACUNA');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('Niño: ' || v_child_name || ' (ID: ' || v_child_id || ')');
    DBMS_OUTPUT.PUT_LINE('Vacuna: ' || v_vaccine_name || ' (ID: ' || v_vaccine_id || ')');
    DBMS_OUTPUT.PUT_LINE('Estado: ' || v_status);
    DBMS_OUTPUT.PUT_LINE('===========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 1.2: ' || SQLERRM);
END;
/

PROMPT
PROMPT ============================================================================
PROMPT   EJERCICIO 2: PROCEDIMIENTOS CON CURSORES
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT Test 2.1: SP_LIST_PENDING_VACCINES - Listar vacunas pendientes
PROMPT ----------------------------------------------------------------------------
DECLARE
    v_child_id NUMBER;
BEGIN
    SELECT MIN(id) INTO v_child_id FROM children WHERE deleted_at IS NULL;

    IF v_child_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay niños disponibles para la prueba');
        RETURN;
    END IF;

    sp_list_pending_vaccines(v_child_id);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 2.1: ' || SQLERRM);
END;
/

PROMPT
PROMPT ----------------------------------------------------------------------------
PROMPT Test 2.2: SP_LIST_APPLIED_VACCINES - Listar vacunas aplicadas
PROMPT ----------------------------------------------------------------------------
DECLARE
    v_child_id NUMBER;
BEGIN
    SELECT MIN(id) INTO v_child_id FROM children WHERE deleted_at IS NULL;

    IF v_child_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay niños disponibles para la prueba');
        RETURN;
    END IF;

    sp_list_applied_vaccines(v_child_id);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 2.2: ' || SQLERRM);
END;
/

PROMPT
PROMPT ============================================================================
PROMPT   EJERCICIO 3: TRIGGERS EN APPOINTMENTS
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT Test 3.1: SP_SHOW_APPOINTMENT_TRIGGERS - Mostrar triggers existentes
PROMPT ----------------------------------------------------------------------------
BEGIN
    sp_show_appointment_triggers;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 3.1: ' || SQLERRM);
END;
/

PROMPT
PROMPT ----------------------------------------------------------------------------
PROMPT Test 3.2: SP_TEST_APPOINTMENT_TRIGGERS - Demostrar funcionamiento
PROMPT ----------------------------------------------------------------------------
BEGIN
    sp_test_appointment_triggers;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test 3.2: ' || SQLERRM);
END;
/

PROMPT
PROMPT ============================================================================
PROMPT   PRUEBAS ADICIONALES - Verificar múltiples niños
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT Test Extra: Listar vacunas para todos los niños
PROMPT ----------------------------------------------------------------------------
DECLARE
    v_count NUMBER := 0;
BEGIN
    FOR rec IN (
        SELECT id, first_name, last_name
        FROM children
        WHERE deleted_at IS NULL
        ORDER BY id
        FETCH FIRST 3 ROWS ONLY  -- Limitar a 3 niños para no saturar output
    ) LOOP
        v_count := v_count + 1;

        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('############################################################');
        DBMS_OUTPUT.PUT_LINE('## NIÑO ' || v_count || ': ' || rec.first_name || ' ' || rec.last_name || ' (ID: ' || rec.id || ')');
        DBMS_OUTPUT.PUT_LINE('############################################################');
        DBMS_OUTPUT.PUT_LINE('');

        -- Listar pendientes
        sp_list_pending_vaccines(rec.id);

        DBMS_OUTPUT.PUT_LINE('');

        -- Listar aplicadas
        sp_list_applied_vaccines(rec.id);

        DBMS_OUTPUT.PUT_LINE('');
    END LOOP;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('⚠ No hay niños disponibles para esta prueba');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en test extra: ' || SQLERRM);
END;
/

PROMPT
PROMPT ============================================================================
PROMPT   RESUMEN DE OBJETOS PL/SQL CREADOS
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT Packages, Procedures y Functions relacionadas con ejercicios:
PROMPT ----------------------------------------------------------------------------

SET HEADING ON;
SET FEEDBACK ON;

COLUMN object_type FORMAT A15;
COLUMN object_name FORMAT A40;
COLUMN status FORMAT A10;
COLUMN created FORMAT A20;

SELECT
    object_type,
    object_name,
    status,
    TO_CHAR(created, 'DD/MM/YYYY HH24:MI') AS created
FROM user_objects
WHERE (
    object_name LIKE '%VACCINATION%'
    OR object_name LIKE '%EXERCISE%'
    OR object_name LIKE '%APPOINTMENT_TRIGGER%'
)
AND object_type IN ('PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'FUNCTION')
ORDER BY object_type, object_name;

PROMPT
PROMPT ----------------------------------------------------------------------------
PROMPT Triggers en la tabla APPOINTMENTS:
PROMPT ----------------------------------------------------------------------------

COLUMN trigger_name FORMAT A30;
COLUMN trigger_type FORMAT A20;
COLUMN triggering_event FORMAT A30;
COLUMN status FORMAT A10;

SELECT
    trigger_name,
    trigger_type,
    triggering_event,
    status
FROM user_triggers
WHERE table_name = 'APPOINTMENTS'
ORDER BY trigger_name;

PROMPT
PROMPT ============================================================================
PROMPT   FIN DE LAS PRUEBAS
PROMPT ============================================================================
PROMPT
PROMPT Todas las pruebas han sido completadas.
PROMPT
PROMPT NOTA: Este script utiliza ROLLBACK para no persistir datos de prueba.
PROMPT       Si necesita guardar alguna cita de prueba, ejecute los comandos
PROMPT       individualmente sin el ROLLBACK.
PROMPT
PROMPT ============================================================================

SET FEEDBACK ON;
SET VERIFY ON;
