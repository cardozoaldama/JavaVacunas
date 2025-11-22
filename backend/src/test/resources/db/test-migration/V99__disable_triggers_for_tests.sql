-- Disable triggers that fail validation in TestContainers
-- These triggers reference tables and users across schemas which don't work in test environment

-- Disable appointment reminder trigger (fails due to notifications table references)
ALTER TRIGGER trg_appointment_reminder DISABLE;

-- Disable inventory alerts trigger (fails due to cross-table references)
ALTER TRIGGER trg_inventory_alerts DISABLE;

-- Disable audit trigger (may fail due to audit_log references)
ALTER TRIGGER trg_audit_vaccination_records DISABLE;

-- Note: Update triggers (trg_*_updated_at) are kept enabled as they don't have complex dependencies
