-- V6: Create quests and quest_completions tables
CREATE TABLE quests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    xp_reward       INTEGER NOT NULL,
    category        VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deadline        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE quest_completions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    quest_id        UUID NOT NULL REFERENCES quests(id),
    completed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_quest UNIQUE (user_id, quest_id)
);

CREATE INDEX idx_quest_completions_user_id ON quest_completions(user_id);
CREATE INDEX idx_quest_completions_quest_id ON quest_completions(quest_id);
CREATE INDEX idx_quests_status ON quests(status);
