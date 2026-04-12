drop extension if exists "pg_net";


create table "public"."achievements" (
                                         "id" uuid not null default gen_random_uuid(),
                                         "code" character varying(50) not null,
                                         "name" character varying(100) not null,
                                         "description" character varying(500) not null,
                                         "icon" character varying(50),
                                         "category" character varying(20) not null,
                                         "requirement_type" character varying(50) not null,
                                         "threshold" integer not null,
                                         "points" integer not null,
                                         "is_active" boolean not null default true
);



create table "public"."activity_log" (
                                         "id" uuid not null default gen_random_uuid(),
                                         "user_id" uuid not null,
                                         "activity_type_id" uuid not null,
                                         "xp_earned" integer not null,
                                         "logged_at" timestamp with time zone not null default now()
);



create table "public"."activity_types" (
                                           "id" uuid not null default gen_random_uuid(),
                                           "name" character varying(100) not null,
                                           "description" text,
                                           "icon" character varying(50),
                                           "category" character varying(30) not null,
                                           "xp_reward" integer not null,
                                           "frequency" character varying(20) not null default 'DAILY'::character varying,
                                           "cooldown_hours" integer not null default 24,
                                           "is_active" boolean not null default true,
                                           "created_at" timestamp with time zone not null default now()
);



create table "public"."comments" (
                                     "id" uuid not null default gen_random_uuid(),
                                     "post_id" uuid,
                                     "user_id" uuid,
                                     "text" text,
                                     "created_at" timestamp without time zone default now()
);



create table "public"."cosmetic_items" (
                                           "id" uuid not null default gen_random_uuid(),
                                           "name" character varying(100) not null,
                                           "description" text,
                                           "preview_url" character varying(500),
                                           "category" character varying(30) not null,
                                           "price" integer not null,
                                           "rarity" character varying(20) not null default 'COMMON'::character varying,
                                           "is_active" boolean not null default true,
                                           "created_at" timestamp with time zone not null default now()
);



create table "public"."feed_events" (
                                        "id" uuid not null default gen_random_uuid(),
                                        "user_id" uuid not null,
                                        "event_type" character varying(30) not null,
                                        "event_data" text,
                                        "created_at" timestamp with time zone not null default now()
);



create table "public"."follows" (
                                    "follower_id" uuid not null,
                                    "following_id" uuid not null,
                                    "created_at" timestamp without time zone default now()
);



create table "public"."friend_requests" (
                                            "id" uuid not null default gen_random_uuid(),
                                            "sender_id" uuid not null,
                                            "receiver_id" uuid not null,
                                            "status" character varying(20) not null default 'PENDING'::character varying,
                                            "created_at" timestamp with time zone not null default now(),
                                            "updated_at" timestamp with time zone
);



create table "public"."friendships" (
                                        "id" uuid not null default gen_random_uuid(),
                                        "user_id" uuid not null,
                                        "friend_id" uuid not null,
                                        "created_at" timestamp with time zone not null default now()
);



create table "public"."leaderboard_snapshots" (
                                                  "id" uuid not null default gen_random_uuid(),
                                                  "user_id" uuid not null,
                                                  "friend_owner_id" uuid not null,
                                                  "position" integer not null,
                                                  "score" bigint not null,
                                                  "snapshot_date" date not null,
                                                  "category" character varying(30)
);



create table "public"."life_stat_history" (
                                              "id" uuid not null default gen_random_uuid(),
                                              "user_id" uuid not null,
                                              "category" character varying(30) not null,
                                              "old_value" integer not null,
                                              "new_value" integer not null,
                                              "reason" text,
                                              "changed_at" timestamp with time zone not null default now()
);



create table "public"."life_stats" (
                                       "id" uuid not null default gen_random_uuid(),
                                       "user_id" uuid not null,
                                       "category" character varying(30) not null,
                                       "value" integer not null,
                                       "last_updated" timestamp with time zone not null default now(),
                                       "metadata" text,
                                       "previous_value" integer
);



create table "public"."likes" (
                                  "id" uuid not null default gen_random_uuid(),
                                  "post_id" uuid,
                                  "user_id" uuid,
                                  "created_at" timestamp without time zone default now()
);



create table "public"."points" (
                                   "user_id" uuid not null,
                                   "points" integer default 0,
                                   "last_updated" timestamp without time zone default now()
);



create table "public"."posts" (
                                  "id" uuid not null default gen_random_uuid(),
                                  "user_id" uuid,
                                  "caption" text,
                                  "media_url" text not null,
                                  "media_type" text,
                                  "created_at" timestamp without time zone default now()
);



create table "public"."quest_completions" (
                                              "id" uuid not null default gen_random_uuid(),
                                              "user_id" uuid not null,
                                              "quest_id" uuid not null,
                                              "completed_at" timestamp with time zone not null default now()
);



create table "public"."quests" (
                                   "id" uuid not null default gen_random_uuid(),
                                   "title" character varying(200) not null,
                                   "description" text,
                                   "xp_reward" integer not null,
                                   "category" character varying(30) not null,
                                   "status" character varying(20) not null default 'ACTIVE'::character varying,
                                   "deadline" timestamp with time zone,
                                   "created_at" timestamp with time zone not null default now()
);



create table "public"."user_achievements" (
                                              "id" uuid not null default gen_random_uuid(),
                                              "user_id" uuid not null,
                                              "achievement_id" uuid not null,
                                              "unlocked_at" timestamp with time zone not null default now()
);



create table "public"."user_cosmetics" (
                                           "id" uuid not null default gen_random_uuid(),
                                           "user_id" uuid not null,
                                           "cosmetic_item_id" uuid not null,
                                           "purchased_at" timestamp with time zone not null default now(),
                                           "is_equipped" boolean not null default false
);



create table "public"."user_displayed_achievements" (
                                                        "user_id" uuid not null,
                                                        "achievement_id" uuid not null
);



create table "public"."user_streaks" (
                                         "id" uuid not null default gen_random_uuid(),
                                         "user_id" uuid not null,
                                         "current_streak" integer not null default 0,
                                         "longest_streak" integer not null default 0,
                                         "last_activity_date" date
);



create table "public"."users" (
                                  "id" uuid not null,
                                  "username" character varying(20),
                                  "email" text not null,
                                  "full_name" character varying(100),
                                  "profile_pic" text,
                                  "created_at" timestamp without time zone not null default now(),
                                  "email_verified" boolean not null,
                                  "profile_complete" boolean not null default false,
                                  "updated_at" timestamp with time zone,
                                  "total_score" bigint not null default 0,
                                  "rank_tier" character varying(20) not null default 'BRONZE'::character varying,
                                  "xp" bigint not null default 0,
                                  "level" integer not null default 1
);


CREATE UNIQUE INDEX achievements_code_key ON public.achievements USING btree (code);

CREATE UNIQUE INDEX achievements_pkey ON public.achievements USING btree (id);

CREATE UNIQUE INDEX activity_log_pkey ON public.activity_log USING btree (id);

CREATE UNIQUE INDEX activity_types_pkey ON public.activity_types USING btree (id);

CREATE UNIQUE INDEX comments_pkey ON public.comments USING btree (id);

CREATE UNIQUE INDEX cosmetic_items_pkey ON public.cosmetic_items USING btree (id);

CREATE UNIQUE INDEX feed_events_pkey ON public.feed_events USING btree (id);

CREATE UNIQUE INDEX follows_pkey ON public.follows USING btree (follower_id, following_id);

CREATE UNIQUE INDEX friend_requests_pkey ON public.friend_requests USING btree (id);

CREATE UNIQUE INDEX friendships_pkey ON public.friendships USING btree (id);

CREATE INDEX idx_achievements_category ON public.achievements USING btree (category);

CREATE INDEX idx_achievements_code ON public.achievements USING btree (code);

CREATE INDEX idx_activity_log_user_id ON public.activity_log USING btree (user_id);

CREATE INDEX idx_activity_log_user_type_time ON public.activity_log USING btree (user_id, activity_type_id, logged_at);

CREATE INDEX idx_activity_types_category ON public.activity_types USING btree (category);

CREATE INDEX idx_activity_types_is_active ON public.activity_types USING btree (is_active);

CREATE INDEX idx_cosmetic_items_category ON public.cosmetic_items USING btree (category);

CREATE INDEX idx_cosmetic_items_is_active ON public.cosmetic_items USING btree (is_active);

CREATE INDEX idx_feed_events_created_at ON public.feed_events USING btree (created_at);

CREATE INDEX idx_feed_events_user_id ON public.feed_events USING btree (user_id);

CREATE INDEX idx_friend_requests_receiver_id ON public.friend_requests USING btree (receiver_id);

CREATE INDEX idx_friend_requests_sender_id ON public.friend_requests USING btree (sender_id);

CREATE INDEX idx_friend_requests_status ON public.friend_requests USING btree (status);

CREATE INDEX idx_friendships_friend_id ON public.friendships USING btree (friend_id);

CREATE INDEX idx_friendships_user_id ON public.friendships USING btree (user_id);

CREATE INDEX idx_leaderboard_snapshots_lookup ON public.leaderboard_snapshots USING btree (friend_owner_id, snapshot_date);

CREATE INDEX idx_leaderboard_snapshots_user ON public.leaderboard_snapshots USING btree (user_id, snapshot_date);

CREATE INDEX idx_life_stat_history_user ON public.life_stat_history USING btree (user_id);

CREATE INDEX idx_life_stat_history_user_category ON public.life_stat_history USING btree (user_id, category);

CREATE INDEX idx_life_stats_user_id ON public.life_stats USING btree (user_id);

CREATE INDEX idx_quest_completions_quest_id ON public.quest_completions USING btree (quest_id);

CREATE INDEX idx_quest_completions_user_id ON public.quest_completions USING btree (user_id);

CREATE INDEX idx_quests_status ON public.quests USING btree (status);

CREATE INDEX idx_user_achievements_user ON public.user_achievements USING btree (user_id);

CREATE INDEX idx_user_cosmetics_user_id ON public.user_cosmetics USING btree (user_id);

CREATE INDEX idx_user_displayed_achievements_user ON public.user_displayed_achievements USING btree (user_id);

CREATE INDEX idx_user_streaks_user_id ON public.user_streaks USING btree (user_id);

CREATE INDEX idx_users_username ON public.users USING btree (username);

CREATE UNIQUE INDEX leaderboard_snapshots_pkey ON public.leaderboard_snapshots USING btree (id);

CREATE UNIQUE INDEX life_stat_history_pkey ON public.life_stat_history USING btree (id);

CREATE UNIQUE INDEX life_stats_pkey ON public.life_stats USING btree (id);

CREATE UNIQUE INDEX life_stats_user_id_category_key ON public.life_stats USING btree (user_id, category);

CREATE UNIQUE INDEX likes_pkey ON public.likes USING btree (id);

CREATE UNIQUE INDEX likes_post_id_user_id_key ON public.likes USING btree (post_id, user_id);

CREATE UNIQUE INDEX points_pkey ON public.points USING btree (user_id);

CREATE UNIQUE INDEX posts_pkey ON public.posts USING btree (id);

CREATE UNIQUE INDEX quest_completions_pkey ON public.quest_completions USING btree (id);

CREATE UNIQUE INDEX quests_pkey ON public.quests USING btree (id);

CREATE UNIQUE INDEX uq_user_achievement ON public.user_achievements USING btree (user_id, achievement_id);

CREATE UNIQUE INDEX uq_user_cosmetic ON public.user_cosmetics USING btree (user_id, cosmetic_item_id);

CREATE UNIQUE INDEX uq_user_friend ON public.friendships USING btree (user_id, friend_id);

CREATE UNIQUE INDEX uq_user_quest ON public.quest_completions USING btree (user_id, quest_id);

CREATE UNIQUE INDEX user_achievements_pkey ON public.user_achievements USING btree (id);

CREATE UNIQUE INDEX user_cosmetics_pkey ON public.user_cosmetics USING btree (id);

CREATE UNIQUE INDEX user_displayed_achievements_pkey ON public.user_displayed_achievements USING btree (user_id, achievement_id);

CREATE UNIQUE INDEX user_streaks_pkey ON public.user_streaks USING btree (id);

CREATE UNIQUE INDEX user_streaks_user_id_key ON public.user_streaks USING btree (user_id);

CREATE UNIQUE INDEX users_email_key ON public.users USING btree (email);

CREATE UNIQUE INDEX users_pkey ON public.users USING btree (id);

CREATE UNIQUE INDEX users_username_key ON public.users USING btree (username);

alter table "public"."achievements" add constraint "achievements_pkey" PRIMARY KEY using index "achievements_pkey";

alter table "public"."activity_log" add constraint "activity_log_pkey" PRIMARY KEY using index "activity_log_pkey";

alter table "public"."activity_types" add constraint "activity_types_pkey" PRIMARY KEY using index "activity_types_pkey";

alter table "public"."comments" add constraint "comments_pkey" PRIMARY KEY using index "comments_pkey";

alter table "public"."cosmetic_items" add constraint "cosmetic_items_pkey" PRIMARY KEY using index "cosmetic_items_pkey";

alter table "public"."feed_events" add constraint "feed_events_pkey" PRIMARY KEY using index "feed_events_pkey";

alter table "public"."follows" add constraint "follows_pkey" PRIMARY KEY using index "follows_pkey";

alter table "public"."friend_requests" add constraint "friend_requests_pkey" PRIMARY KEY using index "friend_requests_pkey";

alter table "public"."friendships" add constraint "friendships_pkey" PRIMARY KEY using index "friendships_pkey";

alter table "public"."leaderboard_snapshots" add constraint "leaderboard_snapshots_pkey" PRIMARY KEY using index "leaderboard_snapshots_pkey";

alter table "public"."life_stat_history" add constraint "life_stat_history_pkey" PRIMARY KEY using index "life_stat_history_pkey";

alter table "public"."life_stats" add constraint "life_stats_pkey" PRIMARY KEY using index "life_stats_pkey";

alter table "public"."likes" add constraint "likes_pkey" PRIMARY KEY using index "likes_pkey";

alter table "public"."points" add constraint "points_pkey" PRIMARY KEY using index "points_pkey";

alter table "public"."posts" add constraint "posts_pkey" PRIMARY KEY using index "posts_pkey";

alter table "public"."quest_completions" add constraint "quest_completions_pkey" PRIMARY KEY using index "quest_completions_pkey";

alter table "public"."quests" add constraint "quests_pkey" PRIMARY KEY using index "quests_pkey";

alter table "public"."user_achievements" add constraint "user_achievements_pkey" PRIMARY KEY using index "user_achievements_pkey";

alter table "public"."user_cosmetics" add constraint "user_cosmetics_pkey" PRIMARY KEY using index "user_cosmetics_pkey";

alter table "public"."user_displayed_achievements" add constraint "user_displayed_achievements_pkey" PRIMARY KEY using index "user_displayed_achievements_pkey";

alter table "public"."user_streaks" add constraint "user_streaks_pkey" PRIMARY KEY using index "user_streaks_pkey";

alter table "public"."users" add constraint "users_pkey" PRIMARY KEY using index "users_pkey";

alter table "public"."achievements" add constraint "achievements_code_key" UNIQUE using index "achievements_code_key";

alter table "public"."activity_log" add constraint "activity_log_activity_type_id_fkey" FOREIGN KEY (activity_type_id) REFERENCES public.activity_types(id) not valid;

alter table "public"."activity_log" validate constraint "activity_log_activity_type_id_fkey";

alter table "public"."activity_log" add constraint "activity_log_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."activity_log" validate constraint "activity_log_user_id_fkey";

alter table "public"."comments" add constraint "comments_post_id_fkey" FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE not valid;

alter table "public"."comments" validate constraint "comments_post_id_fkey";

alter table "public"."comments" add constraint "comments_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."comments" validate constraint "comments_user_id_fkey";

alter table "public"."feed_events" add constraint "feed_events_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."feed_events" validate constraint "feed_events_user_id_fkey";

alter table "public"."follows" add constraint "follows_follower_id_fkey" FOREIGN KEY (follower_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."follows" validate constraint "follows_follower_id_fkey";

alter table "public"."follows" add constraint "follows_following_id_fkey" FOREIGN KEY (following_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."follows" validate constraint "follows_following_id_fkey";

alter table "public"."friend_requests" add constraint "chk_not_self_request" CHECK ((sender_id <> receiver_id)) not valid;

alter table "public"."friend_requests" validate constraint "chk_not_self_request";

alter table "public"."friend_requests" add constraint "friend_requests_receiver_id_fkey" FOREIGN KEY (receiver_id) REFERENCES public.users(id) not valid;

alter table "public"."friend_requests" validate constraint "friend_requests_receiver_id_fkey";

alter table "public"."friend_requests" add constraint "friend_requests_sender_id_fkey" FOREIGN KEY (sender_id) REFERENCES public.users(id) not valid;

alter table "public"."friend_requests" validate constraint "friend_requests_sender_id_fkey";

alter table "public"."friendships" add constraint "chk_not_self_friend" CHECK ((user_id <> friend_id)) not valid;

alter table "public"."friendships" validate constraint "chk_not_self_friend";

alter table "public"."friendships" add constraint "friendships_friend_id_fkey" FOREIGN KEY (friend_id) REFERENCES public.users(id) not valid;

alter table "public"."friendships" validate constraint "friendships_friend_id_fkey";

alter table "public"."friendships" add constraint "friendships_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."friendships" validate constraint "friendships_user_id_fkey";

alter table "public"."friendships" add constraint "uq_user_friend" UNIQUE using index "uq_user_friend";

alter table "public"."leaderboard_snapshots" add constraint "leaderboard_snapshots_friend_owner_id_fkey" FOREIGN KEY (friend_owner_id) REFERENCES public.users(id) not valid;

alter table "public"."leaderboard_snapshots" validate constraint "leaderboard_snapshots_friend_owner_id_fkey";

alter table "public"."leaderboard_snapshots" add constraint "leaderboard_snapshots_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."leaderboard_snapshots" validate constraint "leaderboard_snapshots_user_id_fkey";

alter table "public"."life_stat_history" add constraint "life_stat_history_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."life_stat_history" validate constraint "life_stat_history_user_id_fkey";

alter table "public"."life_stats" add constraint "life_stats_user_id_category_key" UNIQUE using index "life_stats_user_id_category_key";

alter table "public"."life_stats" add constraint "life_stats_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."life_stats" validate constraint "life_stats_user_id_fkey";

alter table "public"."life_stats" add constraint "life_stats_value_check" CHECK (((value >= 1) AND (value <= 100))) not valid;

alter table "public"."life_stats" validate constraint "life_stats_value_check";

alter table "public"."likes" add constraint "likes_post_id_fkey" FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE not valid;

alter table "public"."likes" validate constraint "likes_post_id_fkey";

alter table "public"."likes" add constraint "likes_post_id_user_id_key" UNIQUE using index "likes_post_id_user_id_key";

alter table "public"."likes" add constraint "likes_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."likes" validate constraint "likes_user_id_fkey";

alter table "public"."points" add constraint "points_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."points" validate constraint "points_user_id_fkey";

alter table "public"."posts" add constraint "posts_media_type_check" CHECK ((media_type = ANY (ARRAY['image'::text, 'video'::text]))) not valid;

alter table "public"."posts" validate constraint "posts_media_type_check";

alter table "public"."posts" add constraint "posts_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE not valid;

alter table "public"."posts" validate constraint "posts_user_id_fkey";

alter table "public"."quest_completions" add constraint "quest_completions_quest_id_fkey" FOREIGN KEY (quest_id) REFERENCES public.quests(id) not valid;

alter table "public"."quest_completions" validate constraint "quest_completions_quest_id_fkey";

alter table "public"."quest_completions" add constraint "quest_completions_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."quest_completions" validate constraint "quest_completions_user_id_fkey";

alter table "public"."quest_completions" add constraint "uq_user_quest" UNIQUE using index "uq_user_quest";

alter table "public"."user_achievements" add constraint "uq_user_achievement" UNIQUE using index "uq_user_achievement";

alter table "public"."user_achievements" add constraint "user_achievements_achievement_id_fkey" FOREIGN KEY (achievement_id) REFERENCES public.achievements(id) not valid;

alter table "public"."user_achievements" validate constraint "user_achievements_achievement_id_fkey";

alter table "public"."user_achievements" add constraint "user_achievements_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."user_achievements" validate constraint "user_achievements_user_id_fkey";

alter table "public"."user_cosmetics" add constraint "uq_user_cosmetic" UNIQUE using index "uq_user_cosmetic";

alter table "public"."user_cosmetics" add constraint "user_cosmetics_cosmetic_item_id_fkey" FOREIGN KEY (cosmetic_item_id) REFERENCES public.cosmetic_items(id) not valid;

alter table "public"."user_cosmetics" validate constraint "user_cosmetics_cosmetic_item_id_fkey";

alter table "public"."user_cosmetics" add constraint "user_cosmetics_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."user_cosmetics" validate constraint "user_cosmetics_user_id_fkey";

alter table "public"."user_displayed_achievements" add constraint "user_displayed_achievements_achievement_id_fkey" FOREIGN KEY (achievement_id) REFERENCES public.achievements(id) not valid;

alter table "public"."user_displayed_achievements" validate constraint "user_displayed_achievements_achievement_id_fkey";

alter table "public"."user_displayed_achievements" add constraint "user_displayed_achievements_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."user_displayed_achievements" validate constraint "user_displayed_achievements_user_id_fkey";

alter table "public"."user_streaks" add constraint "user_streaks_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public.users(id) not valid;

alter table "public"."user_streaks" validate constraint "user_streaks_user_id_fkey";

alter table "public"."user_streaks" add constraint "user_streaks_user_id_key" UNIQUE using index "user_streaks_user_id_key";

alter table "public"."users" add constraint "users_email_key" UNIQUE using index "users_email_key";

alter table "public"."users" add constraint "users_username_key" UNIQUE using index "users_username_key";

grant delete on table "public"."achievements" to "anon";

grant insert on table "public"."achievements" to "anon";

grant references on table "public"."achievements" to "anon";

grant select on table "public"."achievements" to "anon";

grant trigger on table "public"."achievements" to "anon";

grant truncate on table "public"."achievements" to "anon";

grant update on table "public"."achievements" to "anon";

grant delete on table "public"."achievements" to "authenticated";

grant insert on table "public"."achievements" to "authenticated";

grant references on table "public"."achievements" to "authenticated";

grant select on table "public"."achievements" to "authenticated";

grant trigger on table "public"."achievements" to "authenticated";

grant truncate on table "public"."achievements" to "authenticated";

grant update on table "public"."achievements" to "authenticated";

grant delete on table "public"."achievements" to "service_role";

grant insert on table "public"."achievements" to "service_role";

grant references on table "public"."achievements" to "service_role";

grant select on table "public"."achievements" to "service_role";

grant trigger on table "public"."achievements" to "service_role";

grant truncate on table "public"."achievements" to "service_role";

grant update on table "public"."achievements" to "service_role";

grant delete on table "public"."activity_log" to "anon";

grant insert on table "public"."activity_log" to "anon";

grant references on table "public"."activity_log" to "anon";

grant select on table "public"."activity_log" to "anon";

grant trigger on table "public"."activity_log" to "anon";

grant truncate on table "public"."activity_log" to "anon";

grant update on table "public"."activity_log" to "anon";

grant delete on table "public"."activity_log" to "authenticated";

grant insert on table "public"."activity_log" to "authenticated";

grant references on table "public"."activity_log" to "authenticated";

grant select on table "public"."activity_log" to "authenticated";

grant trigger on table "public"."activity_log" to "authenticated";

grant truncate on table "public"."activity_log" to "authenticated";

grant update on table "public"."activity_log" to "authenticated";

grant delete on table "public"."activity_log" to "service_role";

grant insert on table "public"."activity_log" to "service_role";

grant references on table "public"."activity_log" to "service_role";

grant select on table "public"."activity_log" to "service_role";

grant trigger on table "public"."activity_log" to "service_role";

grant truncate on table "public"."activity_log" to "service_role";

grant update on table "public"."activity_log" to "service_role";

grant delete on table "public"."activity_types" to "anon";

grant insert on table "public"."activity_types" to "anon";

grant references on table "public"."activity_types" to "anon";

grant select on table "public"."activity_types" to "anon";

grant trigger on table "public"."activity_types" to "anon";

grant truncate on table "public"."activity_types" to "anon";

grant update on table "public"."activity_types" to "anon";

grant delete on table "public"."activity_types" to "authenticated";

grant insert on table "public"."activity_types" to "authenticated";

grant references on table "public"."activity_types" to "authenticated";

grant select on table "public"."activity_types" to "authenticated";

grant trigger on table "public"."activity_types" to "authenticated";

grant truncate on table "public"."activity_types" to "authenticated";

grant update on table "public"."activity_types" to "authenticated";

grant delete on table "public"."activity_types" to "service_role";

grant insert on table "public"."activity_types" to "service_role";

grant references on table "public"."activity_types" to "service_role";

grant select on table "public"."activity_types" to "service_role";

grant trigger on table "public"."activity_types" to "service_role";

grant truncate on table "public"."activity_types" to "service_role";

grant update on table "public"."activity_types" to "service_role";

grant delete on table "public"."comments" to "anon";

grant insert on table "public"."comments" to "anon";

grant references on table "public"."comments" to "anon";

grant select on table "public"."comments" to "anon";

grant trigger on table "public"."comments" to "anon";

grant truncate on table "public"."comments" to "anon";

grant update on table "public"."comments" to "anon";

grant delete on table "public"."comments" to "authenticated";

grant insert on table "public"."comments" to "authenticated";

grant references on table "public"."comments" to "authenticated";

grant select on table "public"."comments" to "authenticated";

grant trigger on table "public"."comments" to "authenticated";

grant truncate on table "public"."comments" to "authenticated";

grant update on table "public"."comments" to "authenticated";

grant delete on table "public"."comments" to "service_role";

grant insert on table "public"."comments" to "service_role";

grant references on table "public"."comments" to "service_role";

grant select on table "public"."comments" to "service_role";

grant trigger on table "public"."comments" to "service_role";

grant truncate on table "public"."comments" to "service_role";

grant update on table "public"."comments" to "service_role";

grant delete on table "public"."cosmetic_items" to "anon";

grant insert on table "public"."cosmetic_items" to "anon";

grant references on table "public"."cosmetic_items" to "anon";

grant select on table "public"."cosmetic_items" to "anon";

grant trigger on table "public"."cosmetic_items" to "anon";

grant truncate on table "public"."cosmetic_items" to "anon";

grant update on table "public"."cosmetic_items" to "anon";

grant delete on table "public"."cosmetic_items" to "authenticated";

grant insert on table "public"."cosmetic_items" to "authenticated";

grant references on table "public"."cosmetic_items" to "authenticated";

grant select on table "public"."cosmetic_items" to "authenticated";

grant trigger on table "public"."cosmetic_items" to "authenticated";

grant truncate on table "public"."cosmetic_items" to "authenticated";

grant update on table "public"."cosmetic_items" to "authenticated";

grant delete on table "public"."cosmetic_items" to "service_role";

grant insert on table "public"."cosmetic_items" to "service_role";

grant references on table "public"."cosmetic_items" to "service_role";

grant select on table "public"."cosmetic_items" to "service_role";

grant trigger on table "public"."cosmetic_items" to "service_role";

grant truncate on table "public"."cosmetic_items" to "service_role";

grant update on table "public"."cosmetic_items" to "service_role";

grant delete on table "public"."feed_events" to "anon";

grant insert on table "public"."feed_events" to "anon";

grant references on table "public"."feed_events" to "anon";

grant select on table "public"."feed_events" to "anon";

grant trigger on table "public"."feed_events" to "anon";

grant truncate on table "public"."feed_events" to "anon";

grant update on table "public"."feed_events" to "anon";

grant delete on table "public"."feed_events" to "authenticated";

grant insert on table "public"."feed_events" to "authenticated";

grant references on table "public"."feed_events" to "authenticated";

grant select on table "public"."feed_events" to "authenticated";

grant trigger on table "public"."feed_events" to "authenticated";

grant truncate on table "public"."feed_events" to "authenticated";

grant update on table "public"."feed_events" to "authenticated";

grant delete on table "public"."feed_events" to "service_role";

grant insert on table "public"."feed_events" to "service_role";

grant references on table "public"."feed_events" to "service_role";

grant select on table "public"."feed_events" to "service_role";

grant trigger on table "public"."feed_events" to "service_role";

grant truncate on table "public"."feed_events" to "service_role";

grant update on table "public"."feed_events" to "service_role";

grant delete on table "public"."follows" to "anon";

grant insert on table "public"."follows" to "anon";

grant references on table "public"."follows" to "anon";

grant select on table "public"."follows" to "anon";

grant trigger on table "public"."follows" to "anon";

grant truncate on table "public"."follows" to "anon";

grant update on table "public"."follows" to "anon";

grant delete on table "public"."follows" to "authenticated";

grant insert on table "public"."follows" to "authenticated";

grant references on table "public"."follows" to "authenticated";

grant select on table "public"."follows" to "authenticated";

grant trigger on table "public"."follows" to "authenticated";

grant truncate on table "public"."follows" to "authenticated";

grant update on table "public"."follows" to "authenticated";

grant delete on table "public"."follows" to "service_role";

grant insert on table "public"."follows" to "service_role";

grant references on table "public"."follows" to "service_role";

grant select on table "public"."follows" to "service_role";

grant trigger on table "public"."follows" to "service_role";

grant truncate on table "public"."follows" to "service_role";

grant update on table "public"."follows" to "service_role";

grant delete on table "public"."friend_requests" to "anon";

grant insert on table "public"."friend_requests" to "anon";

grant references on table "public"."friend_requests" to "anon";

grant select on table "public"."friend_requests" to "anon";

grant trigger on table "public"."friend_requests" to "anon";

grant truncate on table "public"."friend_requests" to "anon";

grant update on table "public"."friend_requests" to "anon";

grant delete on table "public"."friend_requests" to "authenticated";

grant insert on table "public"."friend_requests" to "authenticated";

grant references on table "public"."friend_requests" to "authenticated";

grant select on table "public"."friend_requests" to "authenticated";

grant trigger on table "public"."friend_requests" to "authenticated";

grant truncate on table "public"."friend_requests" to "authenticated";

grant update on table "public"."friend_requests" to "authenticated";

grant delete on table "public"."friend_requests" to "service_role";

grant insert on table "public"."friend_requests" to "service_role";

grant references on table "public"."friend_requests" to "service_role";

grant select on table "public"."friend_requests" to "service_role";

grant trigger on table "public"."friend_requests" to "service_role";

grant truncate on table "public"."friend_requests" to "service_role";

grant update on table "public"."friend_requests" to "service_role";

grant delete on table "public"."friendships" to "anon";

grant insert on table "public"."friendships" to "anon";

grant references on table "public"."friendships" to "anon";

grant select on table "public"."friendships" to "anon";

grant trigger on table "public"."friendships" to "anon";

grant truncate on table "public"."friendships" to "anon";

grant update on table "public"."friendships" to "anon";

grant delete on table "public"."friendships" to "authenticated";

grant insert on table "public"."friendships" to "authenticated";

grant references on table "public"."friendships" to "authenticated";

grant select on table "public"."friendships" to "authenticated";

grant trigger on table "public"."friendships" to "authenticated";

grant truncate on table "public"."friendships" to "authenticated";

grant update on table "public"."friendships" to "authenticated";

grant delete on table "public"."friendships" to "service_role";

grant insert on table "public"."friendships" to "service_role";

grant references on table "public"."friendships" to "service_role";

grant select on table "public"."friendships" to "service_role";

grant trigger on table "public"."friendships" to "service_role";

grant truncate on table "public"."friendships" to "service_role";

grant update on table "public"."friendships" to "service_role";

grant delete on table "public"."leaderboard_snapshots" to "anon";

grant insert on table "public"."leaderboard_snapshots" to "anon";

grant references on table "public"."leaderboard_snapshots" to "anon";

grant select on table "public"."leaderboard_snapshots" to "anon";

grant trigger on table "public"."leaderboard_snapshots" to "anon";

grant truncate on table "public"."leaderboard_snapshots" to "anon";

grant update on table "public"."leaderboard_snapshots" to "anon";

grant delete on table "public"."leaderboard_snapshots" to "authenticated";

grant insert on table "public"."leaderboard_snapshots" to "authenticated";

grant references on table "public"."leaderboard_snapshots" to "authenticated";

grant select on table "public"."leaderboard_snapshots" to "authenticated";

grant trigger on table "public"."leaderboard_snapshots" to "authenticated";

grant truncate on table "public"."leaderboard_snapshots" to "authenticated";

grant update on table "public"."leaderboard_snapshots" to "authenticated";

grant delete on table "public"."leaderboard_snapshots" to "service_role";

grant insert on table "public"."leaderboard_snapshots" to "service_role";

grant references on table "public"."leaderboard_snapshots" to "service_role";

grant select on table "public"."leaderboard_snapshots" to "service_role";

grant trigger on table "public"."leaderboard_snapshots" to "service_role";

grant truncate on table "public"."leaderboard_snapshots" to "service_role";

grant update on table "public"."leaderboard_snapshots" to "service_role";

grant delete on table "public"."life_stat_history" to "anon";

grant insert on table "public"."life_stat_history" to "anon";

grant references on table "public"."life_stat_history" to "anon";

grant select on table "public"."life_stat_history" to "anon";

grant trigger on table "public"."life_stat_history" to "anon";

grant truncate on table "public"."life_stat_history" to "anon";

grant update on table "public"."life_stat_history" to "anon";

grant delete on table "public"."life_stat_history" to "authenticated";

grant insert on table "public"."life_stat_history" to "authenticated";

grant references on table "public"."life_stat_history" to "authenticated";

grant select on table "public"."life_stat_history" to "authenticated";

grant trigger on table "public"."life_stat_history" to "authenticated";

grant truncate on table "public"."life_stat_history" to "authenticated";

grant update on table "public"."life_stat_history" to "authenticated";

grant delete on table "public"."life_stat_history" to "service_role";

grant insert on table "public"."life_stat_history" to "service_role";

grant references on table "public"."life_stat_history" to "service_role";

grant select on table "public"."life_stat_history" to "service_role";

grant trigger on table "public"."life_stat_history" to "service_role";

grant truncate on table "public"."life_stat_history" to "service_role";

grant update on table "public"."life_stat_history" to "service_role";

grant delete on table "public"."life_stats" to "anon";

grant insert on table "public"."life_stats" to "anon";

grant references on table "public"."life_stats" to "anon";

grant select on table "public"."life_stats" to "anon";

grant trigger on table "public"."life_stats" to "anon";

grant truncate on table "public"."life_stats" to "anon";

grant update on table "public"."life_stats" to "anon";

grant delete on table "public"."life_stats" to "authenticated";

grant insert on table "public"."life_stats" to "authenticated";

grant references on table "public"."life_stats" to "authenticated";

grant select on table "public"."life_stats" to "authenticated";

grant trigger on table "public"."life_stats" to "authenticated";

grant truncate on table "public"."life_stats" to "authenticated";

grant update on table "public"."life_stats" to "authenticated";

grant delete on table "public"."life_stats" to "service_role";

grant insert on table "public"."life_stats" to "service_role";

grant references on table "public"."life_stats" to "service_role";

grant select on table "public"."life_stats" to "service_role";

grant trigger on table "public"."life_stats" to "service_role";

grant truncate on table "public"."life_stats" to "service_role";

grant update on table "public"."life_stats" to "service_role";

grant delete on table "public"."likes" to "anon";

grant insert on table "public"."likes" to "anon";

grant references on table "public"."likes" to "anon";

grant select on table "public"."likes" to "anon";

grant trigger on table "public"."likes" to "anon";

grant truncate on table "public"."likes" to "anon";

grant update on table "public"."likes" to "anon";

grant delete on table "public"."likes" to "authenticated";

grant insert on table "public"."likes" to "authenticated";

grant references on table "public"."likes" to "authenticated";

grant select on table "public"."likes" to "authenticated";

grant trigger on table "public"."likes" to "authenticated";

grant truncate on table "public"."likes" to "authenticated";

grant update on table "public"."likes" to "authenticated";

grant delete on table "public"."likes" to "service_role";

grant insert on table "public"."likes" to "service_role";

grant references on table "public"."likes" to "service_role";

grant select on table "public"."likes" to "service_role";

grant trigger on table "public"."likes" to "service_role";

grant truncate on table "public"."likes" to "service_role";

grant update on table "public"."likes" to "service_role";

grant delete on table "public"."points" to "anon";

grant insert on table "public"."points" to "anon";

grant references on table "public"."points" to "anon";

grant select on table "public"."points" to "anon";

grant trigger on table "public"."points" to "anon";

grant truncate on table "public"."points" to "anon";

grant update on table "public"."points" to "anon";

grant delete on table "public"."points" to "authenticated";

grant insert on table "public"."points" to "authenticated";

grant references on table "public"."points" to "authenticated";

grant select on table "public"."points" to "authenticated";

grant trigger on table "public"."points" to "authenticated";

grant truncate on table "public"."points" to "authenticated";

grant update on table "public"."points" to "authenticated";

grant delete on table "public"."points" to "service_role";

grant insert on table "public"."points" to "service_role";

grant references on table "public"."points" to "service_role";

grant select on table "public"."points" to "service_role";

grant trigger on table "public"."points" to "service_role";

grant truncate on table "public"."points" to "service_role";

grant update on table "public"."points" to "service_role";

grant delete on table "public"."posts" to "anon";

grant insert on table "public"."posts" to "anon";

grant references on table "public"."posts" to "anon";

grant select on table "public"."posts" to "anon";

grant trigger on table "public"."posts" to "anon";

grant truncate on table "public"."posts" to "anon";

grant update on table "public"."posts" to "anon";

grant delete on table "public"."posts" to "authenticated";

grant insert on table "public"."posts" to "authenticated";

grant references on table "public"."posts" to "authenticated";

grant select on table "public"."posts" to "authenticated";

grant trigger on table "public"."posts" to "authenticated";

grant truncate on table "public"."posts" to "authenticated";

grant update on table "public"."posts" to "authenticated";

grant delete on table "public"."posts" to "service_role";

grant insert on table "public"."posts" to "service_role";

grant references on table "public"."posts" to "service_role";

grant select on table "public"."posts" to "service_role";

grant trigger on table "public"."posts" to "service_role";

grant truncate on table "public"."posts" to "service_role";

grant update on table "public"."posts" to "service_role";

grant delete on table "public"."quest_completions" to "anon";

grant insert on table "public"."quest_completions" to "anon";

grant references on table "public"."quest_completions" to "anon";

grant select on table "public"."quest_completions" to "anon";

grant trigger on table "public"."quest_completions" to "anon";

grant truncate on table "public"."quest_completions" to "anon";

grant update on table "public"."quest_completions" to "anon";

grant delete on table "public"."quest_completions" to "authenticated";

grant insert on table "public"."quest_completions" to "authenticated";

grant references on table "public"."quest_completions" to "authenticated";

grant select on table "public"."quest_completions" to "authenticated";

grant trigger on table "public"."quest_completions" to "authenticated";

grant truncate on table "public"."quest_completions" to "authenticated";

grant update on table "public"."quest_completions" to "authenticated";

grant delete on table "public"."quest_completions" to "service_role";

grant insert on table "public"."quest_completions" to "service_role";

grant references on table "public"."quest_completions" to "service_role";

grant select on table "public"."quest_completions" to "service_role";

grant trigger on table "public"."quest_completions" to "service_role";

grant truncate on table "public"."quest_completions" to "service_role";

grant update on table "public"."quest_completions" to "service_role";

grant delete on table "public"."quests" to "anon";

grant insert on table "public"."quests" to "anon";

grant references on table "public"."quests" to "anon";

grant select on table "public"."quests" to "anon";

grant trigger on table "public"."quests" to "anon";

grant truncate on table "public"."quests" to "anon";

grant update on table "public"."quests" to "anon";

grant delete on table "public"."quests" to "authenticated";

grant insert on table "public"."quests" to "authenticated";

grant references on table "public"."quests" to "authenticated";

grant select on table "public"."quests" to "authenticated";

grant trigger on table "public"."quests" to "authenticated";

grant truncate on table "public"."quests" to "authenticated";

grant update on table "public"."quests" to "authenticated";

grant delete on table "public"."quests" to "service_role";

grant insert on table "public"."quests" to "service_role";

grant references on table "public"."quests" to "service_role";

grant select on table "public"."quests" to "service_role";

grant trigger on table "public"."quests" to "service_role";

grant truncate on table "public"."quests" to "service_role";

grant update on table "public"."quests" to "service_role";

grant delete on table "public"."user_achievements" to "anon";

grant insert on table "public"."user_achievements" to "anon";

grant references on table "public"."user_achievements" to "anon";

grant select on table "public"."user_achievements" to "anon";

grant trigger on table "public"."user_achievements" to "anon";

grant truncate on table "public"."user_achievements" to "anon";

grant update on table "public"."user_achievements" to "anon";

grant delete on table "public"."user_achievements" to "authenticated";

grant insert on table "public"."user_achievements" to "authenticated";

grant references on table "public"."user_achievements" to "authenticated";

grant select on table "public"."user_achievements" to "authenticated";

grant trigger on table "public"."user_achievements" to "authenticated";

grant truncate on table "public"."user_achievements" to "authenticated";

grant update on table "public"."user_achievements" to "authenticated";

grant delete on table "public"."user_achievements" to "service_role";

grant insert on table "public"."user_achievements" to "service_role";

grant references on table "public"."user_achievements" to "service_role";

grant select on table "public"."user_achievements" to "service_role";

grant trigger on table "public"."user_achievements" to "service_role";

grant truncate on table "public"."user_achievements" to "service_role";

grant update on table "public"."user_achievements" to "service_role";

grant delete on table "public"."user_cosmetics" to "anon";

grant insert on table "public"."user_cosmetics" to "anon";

grant references on table "public"."user_cosmetics" to "anon";

grant select on table "public"."user_cosmetics" to "anon";

grant trigger on table "public"."user_cosmetics" to "anon";

grant truncate on table "public"."user_cosmetics" to "anon";

grant update on table "public"."user_cosmetics" to "anon";

grant delete on table "public"."user_cosmetics" to "authenticated";

grant insert on table "public"."user_cosmetics" to "authenticated";

grant references on table "public"."user_cosmetics" to "authenticated";

grant select on table "public"."user_cosmetics" to "authenticated";

grant trigger on table "public"."user_cosmetics" to "authenticated";

grant truncate on table "public"."user_cosmetics" to "authenticated";

grant update on table "public"."user_cosmetics" to "authenticated";

grant delete on table "public"."user_cosmetics" to "service_role";

grant insert on table "public"."user_cosmetics" to "service_role";

grant references on table "public"."user_cosmetics" to "service_role";

grant select on table "public"."user_cosmetics" to "service_role";

grant trigger on table "public"."user_cosmetics" to "service_role";

grant truncate on table "public"."user_cosmetics" to "service_role";

grant update on table "public"."user_cosmetics" to "service_role";

grant delete on table "public"."user_displayed_achievements" to "anon";

grant insert on table "public"."user_displayed_achievements" to "anon";

grant references on table "public"."user_displayed_achievements" to "anon";

grant select on table "public"."user_displayed_achievements" to "anon";

grant trigger on table "public"."user_displayed_achievements" to "anon";

grant truncate on table "public"."user_displayed_achievements" to "anon";

grant update on table "public"."user_displayed_achievements" to "anon";

grant delete on table "public"."user_displayed_achievements" to "authenticated";

grant insert on table "public"."user_displayed_achievements" to "authenticated";

grant references on table "public"."user_displayed_achievements" to "authenticated";

grant select on table "public"."user_displayed_achievements" to "authenticated";

grant trigger on table "public"."user_displayed_achievements" to "authenticated";

grant truncate on table "public"."user_displayed_achievements" to "authenticated";

grant update on table "public"."user_displayed_achievements" to "authenticated";

grant delete on table "public"."user_displayed_achievements" to "service_role";

grant insert on table "public"."user_displayed_achievements" to "service_role";

grant references on table "public"."user_displayed_achievements" to "service_role";

grant select on table "public"."user_displayed_achievements" to "service_role";

grant trigger on table "public"."user_displayed_achievements" to "service_role";

grant truncate on table "public"."user_displayed_achievements" to "service_role";

grant update on table "public"."user_displayed_achievements" to "service_role";

grant delete on table "public"."user_streaks" to "anon";

grant insert on table "public"."user_streaks" to "anon";

grant references on table "public"."user_streaks" to "anon";

grant select on table "public"."user_streaks" to "anon";

grant trigger on table "public"."user_streaks" to "anon";

grant truncate on table "public"."user_streaks" to "anon";

grant update on table "public"."user_streaks" to "anon";

grant delete on table "public"."user_streaks" to "authenticated";

grant insert on table "public"."user_streaks" to "authenticated";

grant references on table "public"."user_streaks" to "authenticated";

grant select on table "public"."user_streaks" to "authenticated";

grant trigger on table "public"."user_streaks" to "authenticated";

grant truncate on table "public"."user_streaks" to "authenticated";

grant update on table "public"."user_streaks" to "authenticated";

grant delete on table "public"."user_streaks" to "service_role";

grant insert on table "public"."user_streaks" to "service_role";

grant references on table "public"."user_streaks" to "service_role";

grant select on table "public"."user_streaks" to "service_role";

grant trigger on table "public"."user_streaks" to "service_role";

grant truncate on table "public"."user_streaks" to "service_role";

grant update on table "public"."user_streaks" to "service_role";

grant delete on table "public"."users" to "anon";

grant insert on table "public"."users" to "anon";

grant references on table "public"."users" to "anon";

grant select on table "public"."users" to "anon";

grant trigger on table "public"."users" to "anon";

grant truncate on table "public"."users" to "anon";

grant update on table "public"."users" to "anon";

grant delete on table "public"."users" to "authenticated";

grant insert on table "public"."users" to "authenticated";

grant references on table "public"."users" to "authenticated";

grant select on table "public"."users" to "authenticated";

grant trigger on table "public"."users" to "authenticated";

grant truncate on table "public"."users" to "authenticated";

grant update on table "public"."users" to "authenticated";

grant delete on table "public"."users" to "service_role";

grant insert on table "public"."users" to "service_role";

grant references on table "public"."users" to "service_role";

grant select on table "public"."users" to "service_role";

grant trigger on table "public"."users" to "service_role";

grant truncate on table "public"."users" to "service_role";

grant update on table "public"."users" to "service_role";


