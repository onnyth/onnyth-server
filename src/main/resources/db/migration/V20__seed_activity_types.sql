-- V20: Seed initial activity types (25+ activities across 5 stat categories)

-- FITNESS activities
INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours) VALUES
    (gen_random_uuid(), 'Walk 5000 steps', 'Take a walk and hit 5000 steps', '🚶', 'FITNESS', 20, 'DAILY', 24),
    (gen_random_uuid(), 'Run 3km', 'Go for a 3km run', '🏃', 'FITNESS', 25, 'DAILY', 24),
    (gen_random_uuid(), 'Workout 30 min', 'Complete a 30-minute workout session', '💪', 'FITNESS', 30, 'DAILY', 24),
    (gen_random_uuid(), 'Yoga session', 'Practice yoga or stretching', '🧘', 'FITNESS', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Drink 8 glasses of water', 'Stay hydrated throughout the day', '💧', 'FITNESS', 10, 'DAILY', 24);

-- EDUCATION activities
INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours) VALUES
    (gen_random_uuid(), 'Read 10 pages', 'Read 10 pages of a book', '📖', 'EDUCATION', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Learn something new', 'Learn a new concept or skill', '🧠', 'EDUCATION', 20, 'DAILY', 24),
    (gen_random_uuid(), 'Complete a lesson', 'Finish an online course lesson', '🎓', 'EDUCATION', 25, 'DAILY', 24),
    (gen_random_uuid(), 'Practice a skill', 'Spend time practicing a skill', '🎯', 'EDUCATION', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Watch an educational video', 'Watch a documentary or tutorial', '📺', 'EDUCATION', 10, 'DAILY', 24);

-- SOCIAL_INFLUENCE activities
INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours) VALUES
    (gen_random_uuid(), 'Meet a friend', 'Spend quality time with a friend', '🤝', 'SOCIAL_INFLUENCE', 10, 'DAILY', 24),
    (gen_random_uuid(), 'Help someone', 'Do a good deed or help someone out', '❤️', 'SOCIAL_INFLUENCE', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Attend a social event', 'Go to a gathering or community event', '🎉', 'SOCIAL_INFLUENCE', 20, 'WEEKLY', 168),
    (gen_random_uuid(), 'Volunteer', 'Volunteer your time for a cause', '🙌', 'SOCIAL_INFLUENCE', 25, 'WEEKLY', 168),
    (gen_random_uuid(), 'Network with someone new', 'Connect with a new person', '🌐', 'SOCIAL_INFLUENCE', 15, 'DAILY', 24);

-- CAREER activities
INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours) VALUES
    (gen_random_uuid(), 'Complete a work task', 'Finish a meaningful work task', '✅', 'CAREER', 20, 'DAILY', 24),
    (gen_random_uuid(), 'Upskill professionally', 'Work on professional development', '📈', 'CAREER', 25, 'DAILY', 24),
    (gen_random_uuid(), 'Mentor someone', 'Share your knowledge with others', '👨‍🏫', 'CAREER', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Plan tomorrow''s tasks', 'Organize and plan your next day', '📝', 'CAREER', 10, 'DAILY', 24),
    (gen_random_uuid(), 'Review work goals', 'Reflect on your career goals and progress', '🎯', 'CAREER', 15, 'WEEKLY', 168);

-- WEALTH activities
INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours) VALUES
    (gen_random_uuid(), 'Track expenses', 'Log your daily expenses', '📊', 'WEALTH', 10, 'DAILY', 24),
    (gen_random_uuid(), 'Save money', 'Put money aside for savings', '💰', 'WEALTH', 15, 'DAILY', 24),
    (gen_random_uuid(), 'Review budget', 'Review and adjust your budget', '📋', 'WEALTH', 15, 'WEEKLY', 168),
    (gen_random_uuid(), 'Learn about investing', 'Read or learn about investing strategies', '📘', 'WEALTH', 20, 'DAILY', 24),
    (gen_random_uuid(), 'Explore a side project', 'Work on a side hustle or project', '🚀', 'WEALTH', 25, 'DAILY', 24);
