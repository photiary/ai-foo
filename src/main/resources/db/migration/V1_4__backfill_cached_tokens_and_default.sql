UPDATE tb_usage_token SET cached_tokens = 0 WHERE cached_tokens IS NULL;

ALTER TABLE tb_usage_token ALTER COLUMN cached_tokens SET DEFAULT 0;