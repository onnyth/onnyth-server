-- V21: Add structured data fields for improved onboarding quality
-- Part of the onboarding redesign to support verified vs unverified data
-- and full domain-level structured storage.

-- =============================================
-- user_occupation: Add verification + raw fallback columns
-- =============================================
ALTER TABLE user_occupation
    ADD COLUMN IF NOT EXISTS raw_job_title     VARCHAR(150),
    ADD COLUMN IF NOT EXISTS raw_company_name  VARCHAR(200),
    ADD COLUMN IF NOT EXISTS is_verified       BOOLEAN NOT NULL DEFAULT FALSE;

-- =============================================
-- user_wealth: Add missing bracket columns
-- =============================================
ALTER TABLE user_wealth
    ADD COLUMN IF NOT EXISTS monthly_spending_bracket VARCHAR(30);

-- =============================================
-- user_wisdom: Add structured wisdom fields
-- Languages and habits were previously conflated into the 'hobbies' JSONB.
-- We now store them separately for proper scoring and filtering.
-- =============================================
ALTER TABLE user_wisdom
    ADD COLUMN IF NOT EXISTS languages       JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS education_level VARCHAR(30),
    ADD COLUMN IF NOT EXISTS institution_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS graduation_year  INTEGER;

-- Rename the 'hobbies' column to better reflect its actual use (habit IDs).
-- The Java field is renamed to 'habitIds'; column name stays 'hobbies' for DB compat.
-- No rename needed — the entity mapping handles this via @Column(name = "hobbies").

COMMENT ON COLUMN user_occupation.is_verified IS
    'TRUE when job title and company were selected from a structured dataset. Unverified entries receive a lower score multiplier.';

COMMENT ON COLUMN user_wisdom.languages IS
    'ISO 639-1 language codes selected by the user, e.g. ["en", "fr", "ar"]. Max 5.';

COMMENT ON COLUMN user_wisdom.education_level IS
    'Standardized education level key: NONE, HIGH_SCHOOL, ASSOCIATE, BACHELORS, MASTERS, PHD, OTHER.';
