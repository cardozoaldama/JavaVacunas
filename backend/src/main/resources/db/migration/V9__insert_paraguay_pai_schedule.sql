-- Insert vaccines according to Paraguay's PAI (Programa Ampliado de Inmunizaciones)

-- BCG (Tuberculosis)
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('BCG', 'Bacilo Calmette-Guérin', 'Tuberculosis', 1, 0, 'Y');

-- Hepatitis B
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Hepatitis B', 'Hepatitis B recombinante', 'Hepatitis B', 3, 0, 'Y');

-- Pentavalente (DPT+HB+Hib)
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Pentavalente', 'DPT+HB+Hib', 'Difteria, Tos ferina, Tétanos, Hepatitis B, Haemophilus influenzae tipo b', 3, 2, 'Y');

-- Antipoliomielítica IPV
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('IPV', 'Vacuna inactivada contra polio', 'Poliomielitis', 3, 2, 'Y');

-- Antipoliomielítica bOPV
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('bOPV', 'Vacuna oral bivalente contra polio', 'Poliomielitis', 2, 18, 'Y');

-- Rotavirus
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Rotavirus', 'Vacuna contra rotavirus', 'Gastroenteritis por rotavirus', 2, 2, 'Y');

-- Neumococo conjugada
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Neumococo', 'PCV13 - Vacuna conjugada neumocócica', 'Infecciones por neumococo', 3, 2, 'Y');

-- SPR (Triple viral)
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('SPR', 'Triple viral - Sarampión, Paperas, Rubéola', 'Sarampión, Paperas, Rubéola', 2, 12, 'Y');

-- Varicela
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Varicela', 'Vacuna contra varicela', 'Varicela', 1, 18, 'Y');

-- Fiebre Amarilla
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('Fiebre Amarilla', 'Vacuna contra fiebre amarilla', 'Fiebre amarilla', 1, 12, 'Y');

-- DPT (refuerzo)
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('DPT', 'Triple bacteriana - refuerzo', 'Difteria, Tos ferina, Tétanos', 1, 18, 'Y');

-- VPH
INSERT INTO vaccines (name, description, disease_prevented, dose_count, minimum_age_months, is_active)
VALUES ('VPH', 'Virus del Papiloma Humano', 'Cáncer cervical y verrugas genitales', 2, 132, 'Y');

-- Insert vaccination schedule for Paraguay (PAI)

-- Birth (0 months)
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 0, 0, 1, 'Y', 'Aplicar al nacer'
FROM vaccines WHERE name = 'BCG';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 0, 0, 1, 'Y', 'Primera dosis al nacer'
FROM vaccines WHERE name = 'Hepatitis B';

-- 2 months
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 2, 2, 3, 'Y', 'Primera dosis'
FROM vaccines WHERE name = 'Pentavalente';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 2, 2, 3, 'Y', 'Primera dosis'
FROM vaccines WHERE name = 'IPV';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 2, 2, 3, 'Y', 'Primera dosis'
FROM vaccines WHERE name = 'Rotavirus';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 2, 2, 3, 'Y', 'Primera dosis'
FROM vaccines WHERE name = 'Neumococo';

-- 4 months
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 4, 4, 5, 'Y', 'Segunda dosis'
FROM vaccines WHERE name = 'Pentavalente';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 4, 4, 5, 'Y', 'Segunda dosis'
FROM vaccines WHERE name = 'IPV';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 4, 4, 5, 'Y', 'Segunda dosis'
FROM vaccines WHERE name = 'Rotavirus';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 4, 4, 5, 'Y', 'Segunda dosis'
FROM vaccines WHERE name = 'Neumococo';

-- 6 months
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 3, 6, 6, 8, 'Y', 'Tercera dosis'
FROM vaccines WHERE name = 'Pentavalente';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 3, 6, 6, 8, 'Y', 'Tercera dosis'
FROM vaccines WHERE name = 'IPV';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 3, 6, 6, 8, 'Y', 'Tercera dosis'
FROM vaccines WHERE name = 'Neumococo';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 6, 6, 8, 'Y', 'Segunda dosis de Hepatitis B'
FROM vaccines WHERE name = 'Hepatitis B';

-- 12 months
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 12, 12, 15, 'Y', 'Primera dosis'
FROM vaccines WHERE name = 'SPR';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 12, 12, 15, 'Y', 'Dosis única'
FROM vaccines WHERE name = 'Fiebre Amarilla';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 3, 12, 12, 15, 'Y', 'Tercera dosis de Hepatitis B'
FROM vaccines WHERE name = 'Hepatitis B';

-- 18 months
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 18, 18, 24, 'Y', 'Dosis única'
FROM vaccines WHERE name = 'Varicela';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 18, 18, 24, 'Y', 'Segunda dosis (refuerzo)'
FROM vaccines WHERE name = 'SPR';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 18, 18, 24, 'Y', 'Refuerzo'
FROM vaccines WHERE name = 'DPT';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 18, 18, 24, 'Y', 'Primera dosis de refuerzo'
FROM vaccines WHERE name = 'bOPV';

-- 4 years (48 months)
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 48, 48, 60, 'Y', 'Segunda dosis de refuerzo'
FROM vaccines WHERE name = 'bOPV';

-- 11 years (132 months) - HPV (mainly for girls)
INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 1, 132, 132, 144, 'Y', 'Primera dosis - principalmente para niñas'
FROM vaccines WHERE name = 'VPH';

INSERT INTO vaccination_schedules (vaccine_id, dose_number, recommended_age_months, age_range_start_months, age_range_end_months, is_mandatory, notes)
SELECT id, 2, 138, 138, 150, 'Y', 'Segunda dosis - 6 meses después de la primera'
FROM vaccines WHERE name = 'VPH';

COMMIT;
