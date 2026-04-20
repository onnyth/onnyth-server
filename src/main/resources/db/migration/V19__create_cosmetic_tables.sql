-- V19: Create cosmetic_items and user_cosmetics tables
CREATE TABLE cosmetic_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    preview_url     VARCHAR(500),
    category        VARCHAR(30) NOT NULL,
    price           INTEGER NOT NULL,
    rarity          VARCHAR(20) NOT NULL DEFAULT 'COMMON',
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_cosmetics (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    cosmetic_item_id    UUID NOT NULL REFERENCES cosmetic_items(id),
    purchased_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_equipped         BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_user_cosmetic UNIQUE (user_id, cosmetic_item_id)
);

CREATE INDEX idx_cosmetic_items_category ON cosmetic_items(category);
CREATE INDEX idx_cosmetic_items_is_active ON cosmetic_items(is_active);
CREATE INDEX idx_user_cosmetics_user_id ON user_cosmetics(user_id);
