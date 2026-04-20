-- V10: Create achievements table for master achievement definitions
CREATE TABLE achievements (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code              VARCHAR(50) NOT NULL UNIQUE,
    name              VARCHAR(100) NOT NULL,
    description       VARCHAR(500) NOT NULL,
    icon              VARCHAR(50),
    category          VARCHAR(20) NOT NULL,
    requirement_type  VARCHAR(50) NOT NULL,
    threshold         INTEGER NOT NULL,
    points            INTEGER NOT NULL,
    is_active         BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_achievements_category ON achievements(category);
CREATE INDEX idx_achievements_code ON achievements(code);
