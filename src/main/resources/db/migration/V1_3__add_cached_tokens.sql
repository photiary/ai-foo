-- Add cached_tokens column to tb_usage_token for OpenAI cached input tokens
ALTER TABLE tb_usage_token
    ADD COLUMN IF NOT EXISTS cached_tokens INTEGER;