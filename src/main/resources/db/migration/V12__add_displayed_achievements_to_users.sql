-- V12: Create user_displayed_achievements collection table for @ElementCollection
CREATE TABLE user_displayed_achievements (
    user_id           UUID NOT NULL REFERENCES users(id),
    achievement_id    UUID NOT NULL REFERENCES achievements(id),
    PRIMARY KEY (user_id, achievement_id)
);

CREATE INDEX idx_user_displayed_achievements_user ON user_displayed_achievements(user_id);
