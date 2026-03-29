-- V14: Create activity_types table
CREATE TABLE activity_types (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    icon            VARCHAR(50),
    category        VARCHAR(30) NOT NULL,
    xp_reward       INTEGER NOT NULL,
    frequency       VARCHAR(20) NOT NULL DEFAULT 'DAILY',
    cooldown_hours  INTEGER NOT NULL DEFAULT 24,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_types_category ON activity_types(category);
CREATE INDEX idx_activity_types_is_active ON activity_types(is_active);
