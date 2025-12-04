-- Convert vaccination_schedules.is_mandatory from CHAR(1) to NUMBER(1) for Boolean mapping
-- This migration converts 'Y'/'N' character values to 1/0 numeric values for proper Boolean handling

-- Step 1: Add new temporary column as NUMBER(1)
ALTER TABLE vaccination_schedules
ADD is_mandatory_new NUMBER(1);

-- Step 2: Copy and convert data ('Y' -> 1, 'N' -> 0)
UPDATE vaccination_schedules
SET is_mandatory_new = CASE
    WHEN is_mandatory = 'Y' THEN 1
    WHEN is_mandatory = 'N' THEN 0
    ELSE 1  -- Default to true/1 for any unexpected values
END;

-- Step 3: Commit the data conversion
COMMIT;

-- Step 4: Drop the old constraint first (it will be dropped with the column, but explicit is clearer)
ALTER TABLE vaccination_schedules
DROP CONSTRAINT chk_vs_is_mandatory;

-- Step 5: Drop the old CHAR(1) column
ALTER TABLE vaccination_schedules
DROP COLUMN is_mandatory;

-- Step 6: Rename the new column to is_mandatory
ALTER TABLE vaccination_schedules
RENAME COLUMN is_mandatory_new TO is_mandatory;

-- Step 7: Add NOT NULL constraint with default value
ALTER TABLE vaccination_schedules
MODIFY is_mandatory NUMBER(1) DEFAULT 1 NOT NULL;

-- Step 8: Add check constraint to ensure only 0 or 1 values
ALTER TABLE vaccination_schedules
ADD CONSTRAINT chk_vs_is_mandatory CHECK (is_mandatory IN (0, 1));

-- Comments
COMMENT ON COLUMN vaccination_schedules.is_mandatory IS 'Whether vaccine is mandatory in country schedule (0=false, 1=true)';
