.PHONY: dev dev-local down reset

# Start the local Supabase stack (Postgres/Auth/Storage/Studio) and the app container,
# reaching Supabase on the host via host.docker.internal.
dev:
	supabase start
	docker compose up app

# Start the local Supabase stack only; run the app yourself with ./mvnw spring-boot:run
dev-local:
	supabase start

# Stop the app container and the local Supabase stack
down:
	docker compose down
	supabase stop

# Rebuild the local Supabase Postgres schema from scratch using supabase/migrations
reset:
	supabase db reset
