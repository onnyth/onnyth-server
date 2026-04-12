

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE EXTENSION IF NOT EXISTS "pg_graphql" WITH SCHEMA "graphql";






CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA "vault";






CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA "extensions";






CREATE OR REPLACE FUNCTION "public"."update_modified_column"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."update_modified_column"() OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."achievements" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "code" character varying(50) NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" character varying(500) NOT NULL,
    "icon" character varying(50),
    "category" character varying(20) NOT NULL,
    "requirement_type" character varying(50) NOT NULL,
    "threshold" integer NOT NULL,
    "points" integer NOT NULL,
    "is_active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."achievements" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."activity_log" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "activity_type_id" "uuid" NOT NULL,
    "xp_earned" integer NOT NULL,
    "logged_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."activity_log" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."activity_types" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" "text",
    "icon" character varying(50),
    "category" character varying(30) NOT NULL,
    "xp_reward" integer NOT NULL,
    "frequency" character varying(20) DEFAULT 'DAILY'::character varying NOT NULL,
    "cooldown_hours" integer DEFAULT 24 NOT NULL,
    "is_active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."activity_types" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."comments" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "post_id" "uuid" NOT NULL,
    "user_id" "uuid" NOT NULL,
    "text" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."comments" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."cosmetic_items" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "name" character varying(100) NOT NULL,
    "description" "text",
    "preview_url" character varying(500),
    "category" character varying(30) NOT NULL,
    "price" integer NOT NULL,
    "rarity" character varying(20) DEFAULT 'COMMON'::character varying NOT NULL,
    "is_active" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "chk_cosmetic_price" CHECK (("price" >= 0))
);


ALTER TABLE "public"."cosmetic_items" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."feed_events" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "event_type" character varying(30) NOT NULL,
    "event_data" "jsonb",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."feed_events" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."follows" (
    "follower_id" "uuid" NOT NULL,
    "following_id" "uuid" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "chk_not_self_follow" CHECK (("follower_id" <> "following_id"))
);


ALTER TABLE "public"."follows" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."friend_requests" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "sender_id" "uuid" NOT NULL,
    "receiver_id" "uuid" NOT NULL,
    "status" character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone,
    CONSTRAINT "chk_friend_request_status" CHECK ((("status")::"text" = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::"text"[]))),
    CONSTRAINT "chk_not_self_request" CHECK (("sender_id" <> "receiver_id"))
);


ALTER TABLE "public"."friend_requests" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."friendships" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "friend_id" "uuid" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "chk_not_self_friend" CHECK (("user_id" <> "friend_id"))
);


ALTER TABLE "public"."friendships" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."leaderboard_snapshots" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "friend_owner_id" "uuid" NOT NULL,
    "position" integer NOT NULL,
    "score" bigint NOT NULL,
    "snapshot_date" "date" NOT NULL,
    "category" character varying(30),
    CONSTRAINT "chk_snapshot_category" CHECK ((("category" IS NULL) OR (("category")::"text" = ANY ((ARRAY['OCCUPATION'::character varying, 'WEALTH'::character varying, 'PHYSIQUE'::character varying, 'WISDOM'::character varying, 'CHARISMA'::character varying])::"text"[]))))
);


ALTER TABLE "public"."leaderboard_snapshots" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."likes" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "post_id" "uuid" NOT NULL,
    "user_id" "uuid" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."likes" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."posts" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "caption" "text",
    "media_url" "text" NOT NULL,
    "media_type" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "posts_media_type_check" CHECK (("media_type" = ANY (ARRAY['image'::"text", 'video'::"text"])))
);


ALTER TABLE "public"."posts" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."profile_likes" (
    "liker_id" "uuid" NOT NULL,
    "liked_id" "uuid" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "chk_not_self_profile_like" CHECK (("liker_id" <> "liked_id"))
);


ALTER TABLE "public"."profile_likes" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."quest_completions" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "quest_id" "uuid" NOT NULL,
    "completed_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."quest_completions" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."quests" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "title" character varying(200) NOT NULL,
    "description" "text",
    "xp_reward" integer NOT NULL,
    "category" character varying(30) NOT NULL,
    "status" character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    "deadline" timestamp with time zone,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."quests" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."score_history" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "domain" character varying(20) NOT NULL,
    "old_score" integer NOT NULL,
    "new_score" integer NOT NULL,
    "reason" "text",
    "changed_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "score_history_domain_check" CHECK ((("domain")::"text" = ANY ((ARRAY['OCCUPATION'::character varying, 'WEALTH'::character varying, 'PHYSIQUE'::character varying, 'WISDOM'::character varying, 'CHARISMA'::character varying, 'TOTAL'::character varying])::"text"[])))
);


ALTER TABLE "public"."score_history" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."sport_medals" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "sport" character varying(50) NOT NULL,
    "medal_type" character varying(20) NOT NULL,
    "event_name" character varying(200),
    "year" integer,
    "evidence_url" character varying(500),
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "sport_medals_medal_type_check" CHECK ((("medal_type")::"text" = ANY ((ARRAY['GOLD'::character varying, 'SILVER'::character varying, 'BRONZE'::character varying, 'PARTICIPATION'::character varying, 'CHAMPIONSHIP'::character varying])::"text"[]))),
    CONSTRAINT "sport_medals_year_check" CHECK ((("year" IS NULL) OR (("year" >= 1950) AND ("year" <= 2100))))
);


ALTER TABLE "public"."sport_medals" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_achievements" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "achievement_id" "uuid" NOT NULL,
    "unlocked_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."user_achievements" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_charisma" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "onnyth_profile_likes" integer DEFAULT 0 NOT NULL,
    "score" integer DEFAULT 0 NOT NULL,
    "last_social_sync_at" timestamp with time zone,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_charisma_onnyth_profile_likes_check" CHECK (("onnyth_profile_likes" >= 0)),
    CONSTRAINT "user_charisma_score_check" CHECK (("score" >= 0))
);


ALTER TABLE "public"."user_charisma" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_cosmetics" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "cosmetic_item_id" "uuid" NOT NULL,
    "purchased_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "is_equipped" boolean DEFAULT false NOT NULL
);


ALTER TABLE "public"."user_cosmetics" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_displayed_achievements" (
    "user_id" "uuid" NOT NULL,
    "achievement_id" "uuid" NOT NULL
);


ALTER TABLE "public"."user_displayed_achievements" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_education" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "level" character varying(30) NOT NULL,
    "institution" character varying(200) NOT NULL,
    "field_of_study" character varying(100),
    "graduation_year" integer,
    "is_highest" boolean DEFAULT false NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_education_graduation_year_check" CHECK ((("graduation_year" IS NULL) OR (("graduation_year" >= 1950) AND ("graduation_year" <= 2100)))),
    CONSTRAINT "user_education_level_check" CHECK ((("level")::"text" = ANY ((ARRAY['HIGH_SCHOOL'::character varying, 'ASSOCIATE'::character varying, 'BACHELORS'::character varying, 'MASTERS'::character varying, 'PHD'::character varying, 'CERTIFICATION'::character varying, 'BOOTCAMP'::character varying, 'SELF_TAUGHT'::character varying])::"text"[])))
);


ALTER TABLE "public"."user_education" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_occupation" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "job_title" character varying(100),
    "company_name" character varying(150),
    "industry" character varying(50),
    "employment_type" character varying(20),
    "years_experience" integer,
    "skills" "jsonb" DEFAULT '[]'::"jsonb" NOT NULL,
    "is_current" boolean DEFAULT true NOT NULL,
    "score" integer DEFAULT 0 NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_occupation_employment_type_check" CHECK ((("employment_type")::"text" = ANY ((ARRAY['FULL_TIME'::character varying, 'PART_TIME'::character varying, 'FREELANCE'::character varying, 'SELF_EMPLOYED'::character varying, 'UNEMPLOYED'::character varying, 'STUDENT'::character varying])::"text"[]))),
    CONSTRAINT "user_occupation_score_check" CHECK (("score" >= 0)),
    CONSTRAINT "user_occupation_years_experience_check" CHECK (("years_experience" >= 0))
);


ALTER TABLE "public"."user_occupation" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_physique" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "height_cm" numeric(5,1),
    "weight_kg" numeric(5,1),
    "body_fat_pct" numeric(4,1),
    "fitness_level" character varying(20),
    "workout_source" character varying(30),
    "weekly_workouts" integer DEFAULT 0,
    "score" integer DEFAULT 0 NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_physique_body_fat_pct_check" CHECK ((("body_fat_pct" IS NULL) OR (("body_fat_pct" >= (1)::numeric) AND ("body_fat_pct" <= (70)::numeric)))),
    CONSTRAINT "user_physique_fitness_level_check" CHECK ((("fitness_level" IS NULL) OR (("fitness_level")::"text" = ANY ((ARRAY['BEGINNER'::character varying, 'INTERMEDIATE'::character varying, 'ADVANCED'::character varying, 'ATHLETE'::character varying, 'ELITE'::character varying])::"text"[])))),
    CONSTRAINT "user_physique_height_cm_check" CHECK ((("height_cm" IS NULL) OR (("height_cm" >= (50)::numeric) AND ("height_cm" <= (300)::numeric)))),
    CONSTRAINT "user_physique_score_check" CHECK (("score" >= 0)),
    CONSTRAINT "user_physique_weekly_workouts_check" CHECK ((("weekly_workouts" IS NULL) OR (("weekly_workouts" >= 0) AND ("weekly_workouts" <= 21)))),
    CONSTRAINT "user_physique_weight_kg_check" CHECK ((("weight_kg" IS NULL) OR (("weight_kg" >= (10)::numeric) AND ("weight_kg" <= (500)::numeric))))
);


ALTER TABLE "public"."user_physique" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_social_accounts" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "platform" character varying(20) NOT NULL,
    "username" character varying(100),
    "profile_url" character varying(500),
    "follower_count" integer DEFAULT 0 NOT NULL,
    "is_verified" boolean DEFAULT false NOT NULL,
    "verified_at" timestamp with time zone,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_social_accounts_follower_count_check" CHECK (("follower_count" >= 0)),
    CONSTRAINT "user_social_accounts_platform_check" CHECK ((("platform")::"text" = ANY ((ARRAY['INSTAGRAM'::character varying, 'LINKEDIN'::character varying, 'GITHUB'::character varying, 'YOUTUBE'::character varying])::"text"[])))
);


ALTER TABLE "public"."user_social_accounts" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_streaks" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "current_streak" integer DEFAULT 0 NOT NULL,
    "longest_streak" integer DEFAULT 0 NOT NULL,
    "last_activity_date" "date",
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."user_streaks" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_wealth" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "income_bracket" character varying(30),
    "income_verified" boolean DEFAULT false NOT NULL,
    "net_worth_bracket" character varying(30),
    "monthly_saving_pct" integer,
    "score" integer DEFAULT 0 NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_wealth_income_bracket_check" CHECK ((("income_bracket")::"text" = ANY ((ARRAY['UNDER_25K'::character varying, '25K_50K'::character varying, '50K_75K'::character varying, '75K_100K'::character varying, '100K_150K'::character varying, '150K_250K'::character varying, '250K_500K'::character varying, 'OVER_500K'::character varying])::"text"[]))),
    CONSTRAINT "user_wealth_monthly_saving_pct_check" CHECK ((("monthly_saving_pct" IS NULL) OR (("monthly_saving_pct" >= 0) AND ("monthly_saving_pct" <= 100)))),
    CONSTRAINT "user_wealth_score_check" CHECK (("score" >= 0))
);


ALTER TABLE "public"."user_wealth" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_wisdom" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "hobbies" "jsonb" DEFAULT '[]'::"jsonb" NOT NULL,
    "score" integer DEFAULT 0 NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_wisdom_score_check" CHECK (("score" >= 0))
);


ALTER TABLE "public"."user_wisdom" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."user_xfactors" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "type" character varying(30) NOT NULL,
    "title" character varying(200) NOT NULL,
    "description" "text",
    "evidence_url" character varying(500),
    "metric_value" integer,
    "metric_label" character varying(50),
    "is_verified" boolean DEFAULT false NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "user_xfactors_type_check" CHECK ((("type")::"text" = ANY ((ARRAY['YOUTUBE_CHANNEL'::character varying, 'COMPANY_OWNER'::character varying, 'NGO_FOUNDER'::character varying, 'PUBLICATION'::character varying, 'PATENT'::character varying, 'OPEN_SOURCE'::character varying, 'PUBLIC_SPEAKING'::character varying, 'AWARD'::character varying, 'OTHER'::character varying])::"text"[])))
);


ALTER TABLE "public"."user_xfactors" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."users" (
    "id" "uuid" NOT NULL,
    "username" character varying(20),
    "email" "text" NOT NULL,
    "full_name" character varying(100),
    "profile_pic" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "email_verified" boolean NOT NULL,
    "profile_complete" boolean DEFAULT false NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "total_score" bigint DEFAULT 0 NOT NULL,
    "rank_tier" character varying(20) DEFAULT 'BRONZE'::character varying NOT NULL,
    "xp" bigint DEFAULT 0 NOT NULL,
    "level" integer DEFAULT 1 NOT NULL,
    "onnyth_coins" integer DEFAULT 0 NOT NULL,
    CONSTRAINT "chk_level_positive" CHECK (("level" >= 1)),
    CONSTRAINT "chk_onnyth_coins" CHECK (("onnyth_coins" >= 0)),
    CONSTRAINT "chk_total_score_non_negative" CHECK (("total_score" >= 0)),
    CONSTRAINT "chk_xp_non_negative" CHECK (("xp" >= 0))
);


ALTER TABLE "public"."users" OWNER TO "postgres";


COMMENT ON COLUMN "public"."users"."profile_complete" IS 'True when user has set username, full_name, and profile_pic';



COMMENT ON COLUMN "public"."users"."updated_at" IS 'Timestamp of last profile update';



COMMENT ON COLUMN "public"."users"."total_score" IS 'Persisted weighted life score, updated on every stat change';



ALTER TABLE ONLY "public"."achievements"
    ADD CONSTRAINT "achievements_code_key" UNIQUE ("code");



ALTER TABLE ONLY "public"."achievements"
    ADD CONSTRAINT "achievements_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."activity_log"
    ADD CONSTRAINT "activity_log_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."activity_types"
    ADD CONSTRAINT "activity_types_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."comments"
    ADD CONSTRAINT "comments_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."cosmetic_items"
    ADD CONSTRAINT "cosmetic_items_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."feed_events"
    ADD CONSTRAINT "feed_events_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."follows"
    ADD CONSTRAINT "follows_pkey" PRIMARY KEY ("follower_id", "following_id");



ALTER TABLE ONLY "public"."friend_requests"
    ADD CONSTRAINT "friend_requests_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."friendships"
    ADD CONSTRAINT "friendships_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."leaderboard_snapshots"
    ADD CONSTRAINT "leaderboard_snapshots_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."likes"
    ADD CONSTRAINT "likes_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."likes"
    ADD CONSTRAINT "likes_post_id_user_id_key" UNIQUE ("post_id", "user_id");



ALTER TABLE ONLY "public"."posts"
    ADD CONSTRAINT "posts_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."profile_likes"
    ADD CONSTRAINT "profile_likes_pkey" PRIMARY KEY ("liker_id", "liked_id");



ALTER TABLE ONLY "public"."quest_completions"
    ADD CONSTRAINT "quest_completions_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."quests"
    ADD CONSTRAINT "quests_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."score_history"
    ADD CONSTRAINT "score_history_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."sport_medals"
    ADD CONSTRAINT "sport_medals_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_achievements"
    ADD CONSTRAINT "uq_user_achievement" UNIQUE ("user_id", "achievement_id");



ALTER TABLE ONLY "public"."user_cosmetics"
    ADD CONSTRAINT "uq_user_cosmetic" UNIQUE ("user_id", "cosmetic_item_id");



ALTER TABLE ONLY "public"."friendships"
    ADD CONSTRAINT "uq_user_friend" UNIQUE ("user_id", "friend_id");



ALTER TABLE ONLY "public"."quest_completions"
    ADD CONSTRAINT "uq_user_quest" UNIQUE ("user_id", "quest_id");



ALTER TABLE ONLY "public"."user_achievements"
    ADD CONSTRAINT "user_achievements_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_charisma"
    ADD CONSTRAINT "user_charisma_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_charisma"
    ADD CONSTRAINT "user_charisma_user_id_key" UNIQUE ("user_id");



ALTER TABLE ONLY "public"."user_cosmetics"
    ADD CONSTRAINT "user_cosmetics_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_displayed_achievements"
    ADD CONSTRAINT "user_displayed_achievements_pkey" PRIMARY KEY ("user_id", "achievement_id");



ALTER TABLE ONLY "public"."user_education"
    ADD CONSTRAINT "user_education_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_occupation"
    ADD CONSTRAINT "user_occupation_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_physique"
    ADD CONSTRAINT "user_physique_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_physique"
    ADD CONSTRAINT "user_physique_user_id_key" UNIQUE ("user_id");



ALTER TABLE ONLY "public"."user_social_accounts"
    ADD CONSTRAINT "user_social_accounts_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_social_accounts"
    ADD CONSTRAINT "user_social_accounts_user_platform_key" UNIQUE ("user_id", "platform");



ALTER TABLE ONLY "public"."user_streaks"
    ADD CONSTRAINT "user_streaks_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_streaks"
    ADD CONSTRAINT "user_streaks_user_id_key" UNIQUE ("user_id");



ALTER TABLE ONLY "public"."user_wealth"
    ADD CONSTRAINT "user_wealth_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_wealth"
    ADD CONSTRAINT "user_wealth_user_id_key" UNIQUE ("user_id");



ALTER TABLE ONLY "public"."user_wisdom"
    ADD CONSTRAINT "user_wisdom_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."user_wisdom"
    ADD CONSTRAINT "user_wisdom_user_id_key" UNIQUE ("user_id");



ALTER TABLE ONLY "public"."user_xfactors"
    ADD CONSTRAINT "user_xfactors_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."users"
    ADD CONSTRAINT "users_email_key" UNIQUE ("email");



ALTER TABLE ONLY "public"."users"
    ADD CONSTRAINT "users_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."users"
    ADD CONSTRAINT "users_username_key" UNIQUE ("username");



CREATE INDEX "idx_achievements_category" ON "public"."achievements" USING "btree" ("category");



CREATE INDEX "idx_activity_log_user_id" ON "public"."activity_log" USING "btree" ("user_id");



CREATE INDEX "idx_activity_log_user_type_time" ON "public"."activity_log" USING "btree" ("user_id", "activity_type_id", "logged_at");



CREATE INDEX "idx_activity_types_category" ON "public"."activity_types" USING "btree" ("category");



CREATE INDEX "idx_activity_types_is_active" ON "public"."activity_types" USING "btree" ("is_active");



CREATE INDEX "idx_cosmetic_items_category" ON "public"."cosmetic_items" USING "btree" ("category");



CREATE INDEX "idx_cosmetic_items_is_active" ON "public"."cosmetic_items" USING "btree" ("is_active");



CREATE INDEX "idx_feed_events_created_at" ON "public"."feed_events" USING "btree" ("created_at");



CREATE INDEX "idx_feed_events_data" ON "public"."feed_events" USING "gin" ("event_data");



CREATE INDEX "idx_feed_events_user_id" ON "public"."feed_events" USING "btree" ("user_id");



CREATE INDEX "idx_friend_requests_receiver_id" ON "public"."friend_requests" USING "btree" ("receiver_id");



CREATE INDEX "idx_friend_requests_sender_id" ON "public"."friend_requests" USING "btree" ("sender_id");



CREATE INDEX "idx_friend_requests_status" ON "public"."friend_requests" USING "btree" ("status");



CREATE INDEX "idx_friendships_friend_id" ON "public"."friendships" USING "btree" ("friend_id");



CREATE INDEX "idx_friendships_user_id" ON "public"."friendships" USING "btree" ("user_id");



CREATE INDEX "idx_leaderboard_snapshots_lookup" ON "public"."leaderboard_snapshots" USING "btree" ("friend_owner_id", "snapshot_date");



CREATE INDEX "idx_leaderboard_snapshots_user" ON "public"."leaderboard_snapshots" USING "btree" ("user_id", "snapshot_date");



CREATE INDEX "idx_profile_likes_liked" ON "public"."profile_likes" USING "btree" ("liked_id");



CREATE INDEX "idx_quest_completions_quest_id" ON "public"."quest_completions" USING "btree" ("quest_id");



CREATE INDEX "idx_quest_completions_user_id" ON "public"."quest_completions" USING "btree" ("user_id");



CREATE INDEX "idx_quests_status" ON "public"."quests" USING "btree" ("status");



CREATE INDEX "idx_score_history_user" ON "public"."score_history" USING "btree" ("user_id");



CREATE INDEX "idx_score_history_user_domain" ON "public"."score_history" USING "btree" ("user_id", "domain", "changed_at");



CREATE INDEX "idx_sport_medals_user" ON "public"."sport_medals" USING "btree" ("user_id");



CREATE INDEX "idx_user_achievements_user" ON "public"."user_achievements" USING "btree" ("user_id");



CREATE INDEX "idx_user_charisma_user" ON "public"."user_charisma" USING "btree" ("user_id");



CREATE INDEX "idx_user_cosmetics_user_id" ON "public"."user_cosmetics" USING "btree" ("user_id");



CREATE INDEX "idx_user_displayed_achievements_user" ON "public"."user_displayed_achievements" USING "btree" ("user_id");



CREATE INDEX "idx_user_education_user" ON "public"."user_education" USING "btree" ("user_id");



CREATE INDEX "idx_user_occupation_user" ON "public"."user_occupation" USING "btree" ("user_id");



CREATE INDEX "idx_user_physique_user" ON "public"."user_physique" USING "btree" ("user_id");



CREATE INDEX "idx_user_social_accounts_user" ON "public"."user_social_accounts" USING "btree" ("user_id");



CREATE INDEX "idx_user_streaks_user_id" ON "public"."user_streaks" USING "btree" ("user_id");



CREATE INDEX "idx_user_wealth_user" ON "public"."user_wealth" USING "btree" ("user_id");



CREATE INDEX "idx_user_wisdom_user" ON "public"."user_wisdom" USING "btree" ("user_id");



CREATE INDEX "idx_user_xfactors_user" ON "public"."user_xfactors" USING "btree" ("user_id");



CREATE INDEX "idx_users_username" ON "public"."users" USING "btree" ("username");



CREATE UNIQUE INDEX "uq_leaderboard_snapshot" ON "public"."leaderboard_snapshots" USING "btree" ("user_id", "friend_owner_id", "snapshot_date", "category");



CREATE UNIQUE INDEX "uq_pending_friend_request" ON "public"."friend_requests" USING "btree" (LEAST("sender_id", "receiver_id"), GREATEST("sender_id", "receiver_id")) WHERE (("status")::"text" = 'PENDING'::"text");



CREATE UNIQUE INDEX "uq_user_education_highest" ON "public"."user_education" USING "btree" ("user_id") WHERE ("is_highest" = true);



CREATE UNIQUE INDEX "uq_user_occupation_current" ON "public"."user_occupation" USING "btree" ("user_id") WHERE ("is_current" = true);



CREATE OR REPLACE TRIGGER "trg_user_charisma_updated_at" BEFORE UPDATE ON "public"."user_charisma" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_occupation_updated_at" BEFORE UPDATE ON "public"."user_occupation" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_physique_updated_at" BEFORE UPDATE ON "public"."user_physique" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_social_accounts_updated_at" BEFORE UPDATE ON "public"."user_social_accounts" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_streaks_updated_at" BEFORE UPDATE ON "public"."user_streaks" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_wealth_updated_at" BEFORE UPDATE ON "public"."user_wealth" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_user_wisdom_updated_at" BEFORE UPDATE ON "public"."user_wisdom" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



CREATE OR REPLACE TRIGGER "trg_users_updated_at" BEFORE UPDATE ON "public"."users" FOR EACH ROW EXECUTE FUNCTION "public"."update_modified_column"();



ALTER TABLE ONLY "public"."activity_log"
    ADD CONSTRAINT "activity_log_activity_type_id_fkey" FOREIGN KEY ("activity_type_id") REFERENCES "public"."activity_types"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."activity_log"
    ADD CONSTRAINT "activity_log_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."comments"
    ADD CONSTRAINT "comments_post_id_fkey" FOREIGN KEY ("post_id") REFERENCES "public"."posts"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."comments"
    ADD CONSTRAINT "comments_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."feed_events"
    ADD CONSTRAINT "feed_events_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."follows"
    ADD CONSTRAINT "follows_follower_id_fkey" FOREIGN KEY ("follower_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."follows"
    ADD CONSTRAINT "follows_following_id_fkey" FOREIGN KEY ("following_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."friend_requests"
    ADD CONSTRAINT "friend_requests_receiver_id_fkey" FOREIGN KEY ("receiver_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."friend_requests"
    ADD CONSTRAINT "friend_requests_sender_id_fkey" FOREIGN KEY ("sender_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."friendships"
    ADD CONSTRAINT "friendships_friend_id_fkey" FOREIGN KEY ("friend_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."friendships"
    ADD CONSTRAINT "friendships_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."leaderboard_snapshots"
    ADD CONSTRAINT "leaderboard_snapshots_friend_owner_id_fkey" FOREIGN KEY ("friend_owner_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."leaderboard_snapshots"
    ADD CONSTRAINT "leaderboard_snapshots_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."likes"
    ADD CONSTRAINT "likes_post_id_fkey" FOREIGN KEY ("post_id") REFERENCES "public"."posts"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."likes"
    ADD CONSTRAINT "likes_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."posts"
    ADD CONSTRAINT "posts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."profile_likes"
    ADD CONSTRAINT "profile_likes_liked_fkey" FOREIGN KEY ("liked_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."profile_likes"
    ADD CONSTRAINT "profile_likes_liker_fkey" FOREIGN KEY ("liker_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."quest_completions"
    ADD CONSTRAINT "quest_completions_quest_id_fkey" FOREIGN KEY ("quest_id") REFERENCES "public"."quests"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."quest_completions"
    ADD CONSTRAINT "quest_completions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."score_history"
    ADD CONSTRAINT "score_history_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."sport_medals"
    ADD CONSTRAINT "sport_medals_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_achievements"
    ADD CONSTRAINT "user_achievements_achievement_id_fkey" FOREIGN KEY ("achievement_id") REFERENCES "public"."achievements"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_achievements"
    ADD CONSTRAINT "user_achievements_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_charisma"
    ADD CONSTRAINT "user_charisma_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_cosmetics"
    ADD CONSTRAINT "user_cosmetics_cosmetic_item_id_fkey" FOREIGN KEY ("cosmetic_item_id") REFERENCES "public"."cosmetic_items"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_cosmetics"
    ADD CONSTRAINT "user_cosmetics_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_displayed_achievements"
    ADD CONSTRAINT "user_displayed_achievements_achievement_id_fkey" FOREIGN KEY ("achievement_id") REFERENCES "public"."achievements"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_displayed_achievements"
    ADD CONSTRAINT "user_displayed_achievements_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_education"
    ADD CONSTRAINT "user_education_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_occupation"
    ADD CONSTRAINT "user_occupation_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_physique"
    ADD CONSTRAINT "user_physique_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_social_accounts"
    ADD CONSTRAINT "user_social_accounts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_streaks"
    ADD CONSTRAINT "user_streaks_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_wealth"
    ADD CONSTRAINT "user_wealth_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_wisdom"
    ADD CONSTRAINT "user_wisdom_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."user_xfactors"
    ADD CONSTRAINT "user_xfactors_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE;



CREATE POLICY "Anyone can read profiles" ON "public"."users" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read achievements" ON "public"."achievements" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read activity types" ON "public"."activity_types" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read all comments" ON "public"."comments" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read all follows" ON "public"."follows" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read all likes" ON "public"."likes" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read all posts" ON "public"."posts" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read all profile likes" ON "public"."profile_likes" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read cosmetic items" ON "public"."cosmetic_items" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read leaderboard" ON "public"."leaderboard_snapshots" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Read own friend requests" ON "public"."friend_requests" FOR SELECT TO "authenticated" USING ((("auth"."uid"() = "sender_id") OR ("auth"."uid"() = "receiver_id")));



CREATE POLICY "Read own friendships" ON "public"."friendships" FOR SELECT TO "authenticated" USING ((("auth"."uid"() = "user_id") OR ("auth"."uid"() = "friend_id")));



CREATE POLICY "Read quests" ON "public"."quests" FOR SELECT TO "authenticated" USING (true);



CREATE POLICY "Users manage own activity_log" ON "public"."activity_log" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own comments" ON "public"."comments" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own feed_events" ON "public"."feed_events" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own follows" ON "public"."follows" TO "authenticated" USING (("auth"."uid"() = "follower_id"));



CREATE POLICY "Users manage own likes" ON "public"."likes" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own posts" ON "public"."posts" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own profile likes" ON "public"."profile_likes" TO "authenticated" USING (("auth"."uid"() = "liker_id"));



CREATE POLICY "Users manage own quest_completions" ON "public"."quest_completions" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own score_history" ON "public"."score_history" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own sport_medals" ON "public"."sport_medals" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_achievements" ON "public"."user_achievements" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_charisma" ON "public"."user_charisma" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_cosmetics" ON "public"."user_cosmetics" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_displayed_achievements" ON "public"."user_displayed_achievements" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_education" ON "public"."user_education" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_occupation" ON "public"."user_occupation" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_physique" ON "public"."user_physique" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_social_accounts" ON "public"."user_social_accounts" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_streaks" ON "public"."user_streaks" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_wealth" ON "public"."user_wealth" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_wisdom" ON "public"."user_wisdom" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users manage own user_xfactors" ON "public"."user_xfactors" TO "authenticated" USING (("auth"."uid"() = "user_id"));



CREATE POLICY "Users send friend requests" ON "public"."friend_requests" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "sender_id"));



CREATE POLICY "Users update own profile" ON "public"."users" FOR UPDATE TO "authenticated" USING (("auth"."uid"() = "id"));



ALTER TABLE "public"."achievements" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."activity_log" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."activity_types" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."comments" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."cosmetic_items" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."feed_events" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."follows" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."friend_requests" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."friendships" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."leaderboard_snapshots" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."likes" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."posts" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."profile_likes" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."quest_completions" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."quests" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."score_history" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."sport_medals" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_achievements" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_charisma" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_cosmetics" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_displayed_achievements" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_education" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_occupation" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_physique" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_social_accounts" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_streaks" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_wealth" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_wisdom" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."user_xfactors" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."users" ENABLE ROW LEVEL SECURITY;




ALTER PUBLICATION "supabase_realtime" OWNER TO "postgres";


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";

























































































































































GRANT ALL ON FUNCTION "public"."update_modified_column"() TO "anon";
GRANT ALL ON FUNCTION "public"."update_modified_column"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."update_modified_column"() TO "service_role";


















GRANT ALL ON TABLE "public"."achievements" TO "authenticated";
GRANT ALL ON TABLE "public"."achievements" TO "service_role";
GRANT SELECT ON TABLE "public"."achievements" TO "anon";



GRANT ALL ON TABLE "public"."activity_log" TO "authenticated";
GRANT ALL ON TABLE "public"."activity_log" TO "service_role";



GRANT ALL ON TABLE "public"."activity_types" TO "authenticated";
GRANT ALL ON TABLE "public"."activity_types" TO "service_role";
GRANT SELECT ON TABLE "public"."activity_types" TO "anon";



GRANT ALL ON TABLE "public"."comments" TO "authenticated";
GRANT ALL ON TABLE "public"."comments" TO "service_role";



GRANT ALL ON TABLE "public"."cosmetic_items" TO "authenticated";
GRANT ALL ON TABLE "public"."cosmetic_items" TO "service_role";
GRANT SELECT ON TABLE "public"."cosmetic_items" TO "anon";



GRANT ALL ON TABLE "public"."feed_events" TO "authenticated";
GRANT ALL ON TABLE "public"."feed_events" TO "service_role";



GRANT ALL ON TABLE "public"."follows" TO "authenticated";
GRANT ALL ON TABLE "public"."follows" TO "service_role";



GRANT ALL ON TABLE "public"."friend_requests" TO "authenticated";
GRANT ALL ON TABLE "public"."friend_requests" TO "service_role";



GRANT ALL ON TABLE "public"."friendships" TO "authenticated";
GRANT ALL ON TABLE "public"."friendships" TO "service_role";



GRANT ALL ON TABLE "public"."leaderboard_snapshots" TO "authenticated";
GRANT ALL ON TABLE "public"."leaderboard_snapshots" TO "service_role";



GRANT ALL ON TABLE "public"."likes" TO "authenticated";
GRANT ALL ON TABLE "public"."likes" TO "service_role";



GRANT ALL ON TABLE "public"."posts" TO "authenticated";
GRANT ALL ON TABLE "public"."posts" TO "service_role";



GRANT ALL ON TABLE "public"."profile_likes" TO "authenticated";
GRANT ALL ON TABLE "public"."profile_likes" TO "service_role";



GRANT ALL ON TABLE "public"."quest_completions" TO "authenticated";
GRANT ALL ON TABLE "public"."quest_completions" TO "service_role";



GRANT ALL ON TABLE "public"."quests" TO "authenticated";
GRANT ALL ON TABLE "public"."quests" TO "service_role";
GRANT SELECT ON TABLE "public"."quests" TO "anon";



GRANT ALL ON TABLE "public"."score_history" TO "authenticated";
GRANT ALL ON TABLE "public"."score_history" TO "service_role";



GRANT ALL ON TABLE "public"."sport_medals" TO "authenticated";
GRANT ALL ON TABLE "public"."sport_medals" TO "service_role";



GRANT ALL ON TABLE "public"."user_achievements" TO "authenticated";
GRANT ALL ON TABLE "public"."user_achievements" TO "service_role";



GRANT ALL ON TABLE "public"."user_charisma" TO "authenticated";
GRANT ALL ON TABLE "public"."user_charisma" TO "service_role";



GRANT ALL ON TABLE "public"."user_cosmetics" TO "authenticated";
GRANT ALL ON TABLE "public"."user_cosmetics" TO "service_role";



GRANT ALL ON TABLE "public"."user_displayed_achievements" TO "authenticated";
GRANT ALL ON TABLE "public"."user_displayed_achievements" TO "service_role";



GRANT ALL ON TABLE "public"."user_education" TO "authenticated";
GRANT ALL ON TABLE "public"."user_education" TO "service_role";



GRANT ALL ON TABLE "public"."user_occupation" TO "authenticated";
GRANT ALL ON TABLE "public"."user_occupation" TO "service_role";



GRANT ALL ON TABLE "public"."user_physique" TO "authenticated";
GRANT ALL ON TABLE "public"."user_physique" TO "service_role";



GRANT ALL ON TABLE "public"."user_social_accounts" TO "authenticated";
GRANT ALL ON TABLE "public"."user_social_accounts" TO "service_role";



GRANT ALL ON TABLE "public"."user_streaks" TO "authenticated";
GRANT ALL ON TABLE "public"."user_streaks" TO "service_role";



GRANT ALL ON TABLE "public"."user_wealth" TO "authenticated";
GRANT ALL ON TABLE "public"."user_wealth" TO "service_role";



GRANT ALL ON TABLE "public"."user_wisdom" TO "authenticated";
GRANT ALL ON TABLE "public"."user_wisdom" TO "service_role";



GRANT ALL ON TABLE "public"."user_xfactors" TO "authenticated";
GRANT ALL ON TABLE "public"."user_xfactors" TO "service_role";



GRANT ALL ON TABLE "public"."users" TO "authenticated";
GRANT ALL ON TABLE "public"."users" TO "service_role";









ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";






























drop extension if exists "pg_net";

revoke delete on table "public"."achievements" from "anon";

revoke insert on table "public"."achievements" from "anon";

revoke references on table "public"."achievements" from "anon";

revoke trigger on table "public"."achievements" from "anon";

revoke truncate on table "public"."achievements" from "anon";

revoke update on table "public"."achievements" from "anon";

revoke delete on table "public"."activity_log" from "anon";

revoke insert on table "public"."activity_log" from "anon";

revoke references on table "public"."activity_log" from "anon";

revoke select on table "public"."activity_log" from "anon";

revoke trigger on table "public"."activity_log" from "anon";

revoke truncate on table "public"."activity_log" from "anon";

revoke update on table "public"."activity_log" from "anon";

revoke delete on table "public"."activity_types" from "anon";

revoke insert on table "public"."activity_types" from "anon";

revoke references on table "public"."activity_types" from "anon";

revoke trigger on table "public"."activity_types" from "anon";

revoke truncate on table "public"."activity_types" from "anon";

revoke update on table "public"."activity_types" from "anon";

revoke delete on table "public"."comments" from "anon";

revoke insert on table "public"."comments" from "anon";

revoke references on table "public"."comments" from "anon";

revoke select on table "public"."comments" from "anon";

revoke trigger on table "public"."comments" from "anon";

revoke truncate on table "public"."comments" from "anon";

revoke update on table "public"."comments" from "anon";

revoke delete on table "public"."cosmetic_items" from "anon";

revoke insert on table "public"."cosmetic_items" from "anon";

revoke references on table "public"."cosmetic_items" from "anon";

revoke trigger on table "public"."cosmetic_items" from "anon";

revoke truncate on table "public"."cosmetic_items" from "anon";

revoke update on table "public"."cosmetic_items" from "anon";

revoke delete on table "public"."feed_events" from "anon";

revoke insert on table "public"."feed_events" from "anon";

revoke references on table "public"."feed_events" from "anon";

revoke select on table "public"."feed_events" from "anon";

revoke trigger on table "public"."feed_events" from "anon";

revoke truncate on table "public"."feed_events" from "anon";

revoke update on table "public"."feed_events" from "anon";

revoke delete on table "public"."follows" from "anon";

revoke insert on table "public"."follows" from "anon";

revoke references on table "public"."follows" from "anon";

revoke select on table "public"."follows" from "anon";

revoke trigger on table "public"."follows" from "anon";

revoke truncate on table "public"."follows" from "anon";

revoke update on table "public"."follows" from "anon";

revoke delete on table "public"."friend_requests" from "anon";

revoke insert on table "public"."friend_requests" from "anon";

revoke references on table "public"."friend_requests" from "anon";

revoke select on table "public"."friend_requests" from "anon";

revoke trigger on table "public"."friend_requests" from "anon";

revoke truncate on table "public"."friend_requests" from "anon";

revoke update on table "public"."friend_requests" from "anon";

revoke delete on table "public"."friendships" from "anon";

revoke insert on table "public"."friendships" from "anon";

revoke references on table "public"."friendships" from "anon";

revoke select on table "public"."friendships" from "anon";

revoke trigger on table "public"."friendships" from "anon";

revoke truncate on table "public"."friendships" from "anon";

revoke update on table "public"."friendships" from "anon";

revoke delete on table "public"."leaderboard_snapshots" from "anon";

revoke insert on table "public"."leaderboard_snapshots" from "anon";

revoke references on table "public"."leaderboard_snapshots" from "anon";

revoke select on table "public"."leaderboard_snapshots" from "anon";

revoke trigger on table "public"."leaderboard_snapshots" from "anon";

revoke truncate on table "public"."leaderboard_snapshots" from "anon";

revoke update on table "public"."leaderboard_snapshots" from "anon";

revoke delete on table "public"."likes" from "anon";

revoke insert on table "public"."likes" from "anon";

revoke references on table "public"."likes" from "anon";

revoke select on table "public"."likes" from "anon";

revoke trigger on table "public"."likes" from "anon";

revoke truncate on table "public"."likes" from "anon";

revoke update on table "public"."likes" from "anon";

revoke delete on table "public"."posts" from "anon";

revoke insert on table "public"."posts" from "anon";

revoke references on table "public"."posts" from "anon";

revoke select on table "public"."posts" from "anon";

revoke trigger on table "public"."posts" from "anon";

revoke truncate on table "public"."posts" from "anon";

revoke update on table "public"."posts" from "anon";

revoke delete on table "public"."profile_likes" from "anon";

revoke insert on table "public"."profile_likes" from "anon";

revoke references on table "public"."profile_likes" from "anon";

revoke select on table "public"."profile_likes" from "anon";

revoke trigger on table "public"."profile_likes" from "anon";

revoke truncate on table "public"."profile_likes" from "anon";

revoke update on table "public"."profile_likes" from "anon";

revoke delete on table "public"."quest_completions" from "anon";

revoke insert on table "public"."quest_completions" from "anon";

revoke references on table "public"."quest_completions" from "anon";

revoke select on table "public"."quest_completions" from "anon";

revoke trigger on table "public"."quest_completions" from "anon";

revoke truncate on table "public"."quest_completions" from "anon";

revoke update on table "public"."quest_completions" from "anon";

revoke delete on table "public"."quests" from "anon";

revoke insert on table "public"."quests" from "anon";

revoke references on table "public"."quests" from "anon";

revoke trigger on table "public"."quests" from "anon";

revoke truncate on table "public"."quests" from "anon";

revoke update on table "public"."quests" from "anon";

revoke delete on table "public"."score_history" from "anon";

revoke insert on table "public"."score_history" from "anon";

revoke references on table "public"."score_history" from "anon";

revoke select on table "public"."score_history" from "anon";

revoke trigger on table "public"."score_history" from "anon";

revoke truncate on table "public"."score_history" from "anon";

revoke update on table "public"."score_history" from "anon";

revoke delete on table "public"."sport_medals" from "anon";

revoke insert on table "public"."sport_medals" from "anon";

revoke references on table "public"."sport_medals" from "anon";

revoke select on table "public"."sport_medals" from "anon";

revoke trigger on table "public"."sport_medals" from "anon";

revoke truncate on table "public"."sport_medals" from "anon";

revoke update on table "public"."sport_medals" from "anon";

revoke delete on table "public"."user_achievements" from "anon";

revoke insert on table "public"."user_achievements" from "anon";

revoke references on table "public"."user_achievements" from "anon";

revoke select on table "public"."user_achievements" from "anon";

revoke trigger on table "public"."user_achievements" from "anon";

revoke truncate on table "public"."user_achievements" from "anon";

revoke update on table "public"."user_achievements" from "anon";

revoke delete on table "public"."user_charisma" from "anon";

revoke insert on table "public"."user_charisma" from "anon";

revoke references on table "public"."user_charisma" from "anon";

revoke select on table "public"."user_charisma" from "anon";

revoke trigger on table "public"."user_charisma" from "anon";

revoke truncate on table "public"."user_charisma" from "anon";

revoke update on table "public"."user_charisma" from "anon";

revoke delete on table "public"."user_cosmetics" from "anon";

revoke insert on table "public"."user_cosmetics" from "anon";

revoke references on table "public"."user_cosmetics" from "anon";

revoke select on table "public"."user_cosmetics" from "anon";

revoke trigger on table "public"."user_cosmetics" from "anon";

revoke truncate on table "public"."user_cosmetics" from "anon";

revoke update on table "public"."user_cosmetics" from "anon";

revoke delete on table "public"."user_displayed_achievements" from "anon";

revoke insert on table "public"."user_displayed_achievements" from "anon";

revoke references on table "public"."user_displayed_achievements" from "anon";

revoke select on table "public"."user_displayed_achievements" from "anon";

revoke trigger on table "public"."user_displayed_achievements" from "anon";

revoke truncate on table "public"."user_displayed_achievements" from "anon";

revoke update on table "public"."user_displayed_achievements" from "anon";

revoke delete on table "public"."user_education" from "anon";

revoke insert on table "public"."user_education" from "anon";

revoke references on table "public"."user_education" from "anon";

revoke select on table "public"."user_education" from "anon";

revoke trigger on table "public"."user_education" from "anon";

revoke truncate on table "public"."user_education" from "anon";

revoke update on table "public"."user_education" from "anon";

revoke delete on table "public"."user_occupation" from "anon";

revoke insert on table "public"."user_occupation" from "anon";

revoke references on table "public"."user_occupation" from "anon";

revoke select on table "public"."user_occupation" from "anon";

revoke trigger on table "public"."user_occupation" from "anon";

revoke truncate on table "public"."user_occupation" from "anon";

revoke update on table "public"."user_occupation" from "anon";

revoke delete on table "public"."user_physique" from "anon";

revoke insert on table "public"."user_physique" from "anon";

revoke references on table "public"."user_physique" from "anon";

revoke select on table "public"."user_physique" from "anon";

revoke trigger on table "public"."user_physique" from "anon";

revoke truncate on table "public"."user_physique" from "anon";

revoke update on table "public"."user_physique" from "anon";

revoke delete on table "public"."user_social_accounts" from "anon";

revoke insert on table "public"."user_social_accounts" from "anon";

revoke references on table "public"."user_social_accounts" from "anon";

revoke select on table "public"."user_social_accounts" from "anon";

revoke trigger on table "public"."user_social_accounts" from "anon";

revoke truncate on table "public"."user_social_accounts" from "anon";

revoke update on table "public"."user_social_accounts" from "anon";

revoke delete on table "public"."user_streaks" from "anon";

revoke insert on table "public"."user_streaks" from "anon";

revoke references on table "public"."user_streaks" from "anon";

revoke select on table "public"."user_streaks" from "anon";

revoke trigger on table "public"."user_streaks" from "anon";

revoke truncate on table "public"."user_streaks" from "anon";

revoke update on table "public"."user_streaks" from "anon";

revoke delete on table "public"."user_wealth" from "anon";

revoke insert on table "public"."user_wealth" from "anon";

revoke references on table "public"."user_wealth" from "anon";

revoke select on table "public"."user_wealth" from "anon";

revoke trigger on table "public"."user_wealth" from "anon";

revoke truncate on table "public"."user_wealth" from "anon";

revoke update on table "public"."user_wealth" from "anon";

revoke delete on table "public"."user_wisdom" from "anon";

revoke insert on table "public"."user_wisdom" from "anon";

revoke references on table "public"."user_wisdom" from "anon";

revoke select on table "public"."user_wisdom" from "anon";

revoke trigger on table "public"."user_wisdom" from "anon";

revoke truncate on table "public"."user_wisdom" from "anon";

revoke update on table "public"."user_wisdom" from "anon";

revoke delete on table "public"."user_xfactors" from "anon";

revoke insert on table "public"."user_xfactors" from "anon";

revoke references on table "public"."user_xfactors" from "anon";

revoke select on table "public"."user_xfactors" from "anon";

revoke trigger on table "public"."user_xfactors" from "anon";

revoke truncate on table "public"."user_xfactors" from "anon";

revoke update on table "public"."user_xfactors" from "anon";

revoke delete on table "public"."users" from "anon";

revoke insert on table "public"."users" from "anon";

revoke references on table "public"."users" from "anon";

revoke select on table "public"."users" from "anon";

revoke trigger on table "public"."users" from "anon";

revoke truncate on table "public"."users" from "anon";

revoke update on table "public"."users" from "anon";

alter table "public"."friend_requests" drop constraint "chk_friend_request_status";

alter table "public"."leaderboard_snapshots" drop constraint "chk_snapshot_category";

alter table "public"."score_history" drop constraint "score_history_domain_check";

alter table "public"."sport_medals" drop constraint "sport_medals_medal_type_check";

alter table "public"."user_education" drop constraint "user_education_level_check";

alter table "public"."user_occupation" drop constraint "user_occupation_employment_type_check";

alter table "public"."user_physique" drop constraint "user_physique_fitness_level_check";

alter table "public"."user_social_accounts" drop constraint "user_social_accounts_platform_check";

alter table "public"."user_wealth" drop constraint "user_wealth_income_bracket_check";

alter table "public"."user_xfactors" drop constraint "user_xfactors_type_check";

alter table "public"."friend_requests" add constraint "chk_friend_request_status" CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[]))) not valid;

alter table "public"."friend_requests" validate constraint "chk_friend_request_status";

alter table "public"."leaderboard_snapshots" add constraint "chk_snapshot_category" CHECK (((category IS NULL) OR ((category)::text = ANY ((ARRAY['OCCUPATION'::character varying, 'WEALTH'::character varying, 'PHYSIQUE'::character varying, 'WISDOM'::character varying, 'CHARISMA'::character varying])::text[])))) not valid;

alter table "public"."leaderboard_snapshots" validate constraint "chk_snapshot_category";

alter table "public"."score_history" add constraint "score_history_domain_check" CHECK (((domain)::text = ANY ((ARRAY['OCCUPATION'::character varying, 'WEALTH'::character varying, 'PHYSIQUE'::character varying, 'WISDOM'::character varying, 'CHARISMA'::character varying, 'TOTAL'::character varying])::text[]))) not valid;

alter table "public"."score_history" validate constraint "score_history_domain_check";

alter table "public"."sport_medals" add constraint "sport_medals_medal_type_check" CHECK (((medal_type)::text = ANY ((ARRAY['GOLD'::character varying, 'SILVER'::character varying, 'BRONZE'::character varying, 'PARTICIPATION'::character varying, 'CHAMPIONSHIP'::character varying])::text[]))) not valid;

alter table "public"."sport_medals" validate constraint "sport_medals_medal_type_check";

alter table "public"."user_education" add constraint "user_education_level_check" CHECK (((level)::text = ANY ((ARRAY['HIGH_SCHOOL'::character varying, 'ASSOCIATE'::character varying, 'BACHELORS'::character varying, 'MASTERS'::character varying, 'PHD'::character varying, 'CERTIFICATION'::character varying, 'BOOTCAMP'::character varying, 'SELF_TAUGHT'::character varying])::text[]))) not valid;

alter table "public"."user_education" validate constraint "user_education_level_check";

alter table "public"."user_occupation" add constraint "user_occupation_employment_type_check" CHECK (((employment_type)::text = ANY ((ARRAY['FULL_TIME'::character varying, 'PART_TIME'::character varying, 'FREELANCE'::character varying, 'SELF_EMPLOYED'::character varying, 'UNEMPLOYED'::character varying, 'STUDENT'::character varying])::text[]))) not valid;

alter table "public"."user_occupation" validate constraint "user_occupation_employment_type_check";

alter table "public"."user_physique" add constraint "user_physique_fitness_level_check" CHECK (((fitness_level IS NULL) OR ((fitness_level)::text = ANY ((ARRAY['BEGINNER'::character varying, 'INTERMEDIATE'::character varying, 'ADVANCED'::character varying, 'ATHLETE'::character varying, 'ELITE'::character varying])::text[])))) not valid;

alter table "public"."user_physique" validate constraint "user_physique_fitness_level_check";

alter table "public"."user_social_accounts" add constraint "user_social_accounts_platform_check" CHECK (((platform)::text = ANY ((ARRAY['INSTAGRAM'::character varying, 'LINKEDIN'::character varying, 'GITHUB'::character varying, 'YOUTUBE'::character varying])::text[]))) not valid;

alter table "public"."user_social_accounts" validate constraint "user_social_accounts_platform_check";

alter table "public"."user_wealth" add constraint "user_wealth_income_bracket_check" CHECK (((income_bracket)::text = ANY ((ARRAY['UNDER_25K'::character varying, '25K_50K'::character varying, '50K_75K'::character varying, '75K_100K'::character varying, '100K_150K'::character varying, '150K_250K'::character varying, '250K_500K'::character varying, 'OVER_500K'::character varying])::text[]))) not valid;

alter table "public"."user_wealth" validate constraint "user_wealth_income_bracket_check";

alter table "public"."user_xfactors" add constraint "user_xfactors_type_check" CHECK (((type)::text = ANY ((ARRAY['YOUTUBE_CHANNEL'::character varying, 'COMPANY_OWNER'::character varying, 'NGO_FOUNDER'::character varying, 'PUBLICATION'::character varying, 'PATENT'::character varying, 'OPEN_SOURCE'::character varying, 'PUBLIC_SPEAKING'::character varying, 'AWARD'::character varying, 'OTHER'::character varying])::text[]))) not valid;

alter table "public"."user_xfactors" validate constraint "user_xfactors_type_check";


