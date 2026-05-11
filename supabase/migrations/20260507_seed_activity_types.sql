-- ============================================================
-- Seed: Activity Types
-- Populates the activity_types table with the 35 predefined
-- activities across 5 domains (FITNESS, EDUCATION, SOCIAL_INFLUENCE,
-- OCCUPATION, WEALTH).
-- ============================================================

-- ═══════════ FITNESS ═══════════

INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours, is_active, created_at) VALUES
  (gen_random_uuid(), 'Walk 5000 Steps', 'Get your daily steps in', '🚶', 'FITNESS', 20, 'DAILY', 24, true, now()),
  (gen_random_uuid(), '30 Min Workout', 'Complete a 30-minute exercise session', '💪', 'FITNESS', 25, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Yoga Session', 'Practice yoga or stretching', '🧘', 'FITNESS', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Drink 8 Glasses of Water', 'Stay hydrated throughout the day', '💧', 'FITNESS', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Healthy Meal', 'Prepare and eat a nutritious meal', '🥗', 'FITNESS', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Run 10K', 'Complete a 10-kilometer run', '🏃', 'FITNESS', 50, 'WEEKLY', 168, true, now()),
  (gen_random_uuid(), 'Try a New Sport', 'Play a sport you haven''t tried before', '🎾', 'FITNESS', 40, 'WEEKLY', 168, true, now());

-- ═══════════ EDUCATION ═══════════

INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours, is_active, created_at) VALUES
  (gen_random_uuid(), 'Read 10 Pages', 'Read at least 10 pages of any book', '📖', 'EDUCATION', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Learn Something New', 'Study a topic you didn''t know about', '💡', 'EDUCATION', 20, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Practice a Skill', 'Dedicate time to practicing a skill', '🎯', 'EDUCATION', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Watch Educational Content', 'Watch a lecture, tutorial, or documentary', '🎬', 'EDUCATION', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Write or Journal', 'Write in a journal or work on creative writing', '✍️', 'EDUCATION', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Complete Course Module', 'Finish a module in an online course', '🎓', 'EDUCATION', 50, 'WEEKLY', 168, true, now()),
  (gen_random_uuid(), 'Read a Full Book', 'Finish an entire book', '📚', 'EDUCATION', 60, 'WEEKLY', 168, true, now());

-- ═══════════ SOCIAL_INFLUENCE ═══════════

INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours, is_active, created_at) VALUES
  (gen_random_uuid(), 'Meet a Friend', 'Spend quality time with a friend', '👋', 'SOCIAL_INFLUENCE', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Help Someone', 'Assist someone or do a favor', '🫂', 'SOCIAL_INFLUENCE', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Post Content', 'Share meaningful content online', '📱', 'SOCIAL_INFLUENCE', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Call Family', 'Have a phone or video call with family', '📞', 'SOCIAL_INFLUENCE', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Support a Cause', 'Volunteer, donate, or advocate for a cause', '❤️', 'SOCIAL_INFLUENCE', 20, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Attend a Social Event', 'Go to a gathering, party, or community event', '🎉', 'SOCIAL_INFLUENCE', 40, 'WEEKLY', 168, true, now()),
  (gen_random_uuid(), 'Explore a New Place', 'Visit somewhere you''ve never been', '🗺️', 'SOCIAL_INFLUENCE', 35, 'WEEKLY', 168, true, now());

-- ═══════════ OCCUPATION ═══════════

INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours, is_active, created_at) VALUES
  (gen_random_uuid(), 'Complete a Work Task', 'Finish a meaningful work assignment', '✅', 'OCCUPATION', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Network with Colleague', 'Have a professional conversation or coffee chat', '🤝', 'OCCUPATION', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Learn Industry News', 'Read articles about your field', '📰', 'OCCUPATION', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Update Resume/Portfolio', 'Improve your professional materials', '📝', 'OCCUPATION', 20, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Mentor or Teach', 'Share your knowledge with someone', '🧑‍🏫', 'OCCUPATION', 20, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Complete a Project Milestone', 'Deliver a significant piece of work', '🏁', 'OCCUPATION', 50, 'WEEKLY', 168, true, now()),
  (gen_random_uuid(), 'Attend Professional Event', 'Conference, webinar, or industry meetup', '🎤', 'OCCUPATION', 45, 'WEEKLY', 168, true, now());

-- ═══════════ WEALTH ═══════════

INSERT INTO activity_types (id, name, description, icon, category, xp_reward, frequency, cooldown_hours, is_active, created_at) VALUES
  (gen_random_uuid(), 'Track Expenses', 'Log and categorize your spending', '📊', 'WEALTH', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Save Money', 'Set aside savings for the day', '🏦', 'WEALTH', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Review Investments', 'Check and analyze your investment portfolio', '📈', 'WEALTH', 10, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Avoid Impulse Purchase', 'Resist an unnecessary purchase today', '🛡️', 'WEALTH', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Learn About Finance', 'Read or watch content about personal finance', '💰', 'WEALTH', 15, 'DAILY', 24, true, now()),
  (gen_random_uuid(), 'Create Budget Plan', 'Draft or update your monthly budget', '📋', 'WEALTH', 40, 'WEEKLY', 168, true, now()),
  (gen_random_uuid(), 'Research Investment Options', 'Evaluate new investment opportunities', '🔍', 'WEALTH', 35, 'WEEKLY', 168, true, now());
