-- Normalize analysis_mode values to codes and set default to IMG_SUGG
-- Compatible with PostgreSQL and H2

-- Backfill/convert legacy enum names to codes
UPDATE tb_analysis_food SET analysis_mode = 'IMG_ONLY' WHERE analysis_mode IN ('IMAGE_ONLY', '식사 이미지만 분석');
UPDATE tb_analysis_food SET analysis_mode = 'IMG_SUGG' WHERE analysis_mode IN ('IMAGE_WITH_SUGGESTION', '식사 이미지 분석과 사용자 상태에 따라 제안');

-- Set default to IMG_SUGG for future inserts
ALTER TABLE tb_analysis_food ALTER COLUMN analysis_mode SET DEFAULT 'IMG_SUGG';
