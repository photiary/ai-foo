-- Add analysis_mode column if it doesn't exist and backfill existing rows
-- Compatible with PostgreSQL and H2
ALTER TABLE tb_analysis_food ADD COLUMN IF NOT EXISTS analysis_mode VARCHAR(40);

-- Backfill existing rows: all legacy rows were created for 'image with suggestion'
UPDATE tb_analysis_food SET analysis_mode = 'IMAGE_WITH_SUGGESTION' WHERE analysis_mode IS NULL;

-- Enforce NOT NULL and set default for future inserts
ALTER TABLE tb_analysis_food ALTER COLUMN analysis_mode SET DEFAULT 'IMAGE_WITH_SUGGESTION';

-- In PostgreSQL, enforce NOT NULL safely
-- For H2 this also works when all rows are set
ALTER TABLE tb_analysis_food ALTER COLUMN analysis_mode SET NOT NULL;
