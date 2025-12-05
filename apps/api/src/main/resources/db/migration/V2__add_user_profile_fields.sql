-- Add profile fields to ms_user table
ALTER TABLE ms_user ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE ms_user ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE ms_user ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE ms_user ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(5) DEFAULT 'FR';

-- Add comment for documentation
COMMENT ON COLUMN ms_user.first_name IS 'User first name';
COMMENT ON COLUMN ms_user.last_name IS 'User last name';
COMMENT ON COLUMN ms_user.birth_date IS 'User date of birth';
COMMENT ON COLUMN ms_user.preferred_language IS 'Preferred language code (FR, EN, PT, ES, AR, ZH, DE, IT)';
