-- Create PL/SQL procedures for vaccination exercises
-- Exercise 2: Procedures to list pending and applied vaccines using cursors

-- Procedure to list pending vaccines for a child
CREATE OR REPLACE PROCEDURE sp_list_pending_vaccines(
    p_child_id IN NUMBER
) AS
    v_child_name VARCHAR2(200);
    v_age_months NUMBER;
    v_count NUMBER := 0;

    -- Explicit cursor to iterate through pending vaccines
    CURSOR c_pending_vaccines IS
        SELECT
            v.id AS vaccine_id,
            v.name AS vaccine_name,
            v.disease_prevented,
            vs.recommended_age_months AS scheduled_age,
            CASE
                WHEN v_age_months > (vs.recommended_age_months + 1) THEN 'VENCIDA'
                ELSE 'PENDIENTE'
            END AS status
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
        ORDER BY vs.recommended_age_months, v.name;

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
            DBMS_OUTPUT.PUT_LINE('ERROR: Niño no encontrado con ID ' || p_child_id);
            RETURN;
    END;

    -- Calculate child age in months
    v_age_months := fn_get_child_age_months(p_child_id);

    IF v_age_months IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: No se pudo calcular la edad del niño');
        RETURN;
    END IF;

    -- Print report header
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('      REPORTE DE VACUNAS PENDIENTES');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('Niño: ' || v_child_name);
    DBMS_OUTPUT.PUT_LINE('Edad: ' || v_age_months || ' meses');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Iterate through pending vaccines using cursor
    FOR rec IN c_pending_vaccines LOOP
        v_count := v_count + 1;

        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.vaccine_name);
        DBMS_OUTPUT.PUT_LINE('   Enfermedad: ' || rec.disease_prevented);
        DBMS_OUTPUT.PUT_LINE('   Edad programada: ' || rec.scheduled_age || ' meses');
        DBMS_OUTPUT.PUT_LINE('   Estado: ' || rec.status);
        DBMS_OUTPUT.PUT_LINE('');
    END LOOP;

    -- Print summary
    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay vacunas pendientes para este niño.');
        DBMS_OUTPUT.PUT_LINE('');
    END IF;

    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('Total de Vacunas Pendientes: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('===========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        RAISE;
END sp_list_pending_vaccines;
/

-- Procedure to list applied vaccines for a child
CREATE OR REPLACE PROCEDURE sp_list_applied_vaccines(
    p_child_id IN NUMBER
) AS
    v_child_name VARCHAR2(200);
    v_age_months NUMBER;
    v_count NUMBER := 0;

    -- Explicit cursor to iterate through applied vaccines
    CURSOR c_applied_vaccines IS
        SELECT
            vr.id AS record_id,
            v.name AS vaccine_name,
            v.disease_prevented,
            vr.administration_date,
            vr.dose_number,
            vr.batch_number,
            u.first_name || ' ' || u.last_name AS administered_by_name,
            vr.administration_site,
            vr.notes
        FROM vaccination_records vr
        JOIN vaccines v ON v.id = vr.vaccine_id
        JOIN users u ON u.id = vr.administered_by
        WHERE vr.child_id = p_child_id
        ORDER BY vr.administration_date DESC, v.name;

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
            DBMS_OUTPUT.PUT_LINE('ERROR: Niño no encontrado con ID ' || p_child_id);
            RETURN;
    END;

    -- Calculate child age in months
    v_age_months := fn_get_child_age_months(p_child_id);

    -- Print report header
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('      REPORTE DE VACUNAS APLICADAS');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('Niño: ' || v_child_name);
    DBMS_OUTPUT.PUT_LINE('Edad: ' || v_age_months || ' meses');
    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Iterate through applied vaccines using cursor
    FOR rec IN c_applied_vaccines LOOP
        v_count := v_count + 1;

        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || rec.vaccine_name || ' (Dosis #' || rec.dose_number || ')');
        DBMS_OUTPUT.PUT_LINE('   Enfermedad: ' || rec.disease_prevented);
        DBMS_OUTPUT.PUT_LINE('   Fecha: ' || TO_CHAR(rec.administration_date, 'DD/MM/YYYY'));
        DBMS_OUTPUT.PUT_LINE('   Lote: ' || rec.batch_number);
        DBMS_OUTPUT.PUT_LINE('   Administrado por: ' || rec.administered_by_name);

        IF rec.administration_site IS NOT NULL THEN
            DBMS_OUTPUT.PUT_LINE('   Sitio: ' || rec.administration_site);
        END IF;

        IF rec.notes IS NOT NULL THEN
            DBMS_OUTPUT.PUT_LINE('   Notas: ' || rec.notes);
        END IF;

        DBMS_OUTPUT.PUT_LINE('');
    END LOOP;

    -- Print summary
    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay vacunas aplicadas para este niño.');
        DBMS_OUTPUT.PUT_LINE('');
    END IF;

    DBMS_OUTPUT.PUT_LINE('===========================================');
    DBMS_OUTPUT.PUT_LINE('Total de Vacunas Aplicadas: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('===========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        RAISE;
END sp_list_applied_vaccines;
/
