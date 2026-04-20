-- V13: Seed 12 initial achievements across 5 categories
INSERT INTO achievements (id, code, name, description, icon, category, requirement_type, threshold, points) VALUES
-- STATS (stat value thresholds)
(gen_random_uuid(), 'STAT_CAREER_50',    'Career Starter',      'Reach 50 in Career stat',            '💼', 'STATS', 'STAT_VALUE_CAREER',    50,  10),
(gen_random_uuid(), 'STAT_FITNESS_50',   'Fitness Enthusiast',  'Reach 50 in Fitness stat',           '💪', 'STATS', 'STAT_VALUE_FITNESS',   50,  10),
(gen_random_uuid(), 'STAT_ALL_30',       'Well Rounded',        'Reach 30 in all stat categories',    '🌟', 'STATS', 'ALL_STATS_MIN',        30,  25),

-- SOCIAL (friend count)
(gen_random_uuid(), 'SOCIAL_FIRST_FRIEND', 'First Friend',      'Add your first friend',              '🤝', 'SOCIAL', 'FRIEND_COUNT',         1,   5),
(gen_random_uuid(), 'SOCIAL_5_FRIENDS',    'Social Butterfly',  'Have 5 friends',                     '🦋', 'SOCIAL', 'FRIEND_COUNT',         5,  15),

-- STREAK (consecutive updates — placeholder for future)
(gen_random_uuid(), 'STREAK_7_DAYS',     'Week Warrior',        'Update stats 7 days in a row',       '🔥', 'STREAK', 'UPDATE_STREAK',        7,  20),
(gen_random_uuid(), 'STREAK_30_DAYS',    'Monthly Master',      'Update stats 30 days in a row',      '🏆', 'STREAK', 'UPDATE_STREAK',       30,  50),

-- MILESTONE (total score)
(gen_random_uuid(), 'SCORE_100',         'Century Club',        'Reach a total score of 100',         '💯', 'MILESTONE', 'TOTAL_SCORE',       100,  10),
(gen_random_uuid(), 'SCORE_500',         'High Achiever',       'Reach a total score of 500',         '⭐', 'MILESTONE', 'TOTAL_SCORE',       500,  25),
(gen_random_uuid(), 'RANK_GOLD',         'Gold Standard',       'Reach Gold rank tier',               '🥇', 'MILESTONE', 'RANK_TIER',           3,  30),

-- SPECIAL
(gen_random_uuid(), 'PROFILE_COMPLETE',  'Identity Established','Complete your profile',              '📝', 'SPECIAL', 'PROFILE_COMPLETE',     1,   5),
(gen_random_uuid(), 'FIRST_STAT',        'Taking the First Step','Input your first life stat',        '🚀', 'SPECIAL', 'ANY_STAT_INPUT',       1,   5);
