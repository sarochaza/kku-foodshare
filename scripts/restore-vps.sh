#!/usr/bin/env bash
set -Eeuo pipefail

backup_dir="${1:-}"
if [[ -z "$backup_dir" || ! -d "$backup_dir" ]]; then
  echo "Usage: $0 /path/to/foodshare-backup-folder" >&2
  exit 2
fi
if [[ ! -f "$backup_dir/foodshare.dump" || ! -d "$backup_dir/uploads" ]]; then
  echo 'Backup folder must contain foodshare.dump and uploads/.' >&2
  exit 2
fi

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"
project_name="${FOODSHARE_COMPOSE_PROJECT:-kku-foodshare-prod}"
compose=(docker compose -p "$project_name" -f docker-compose.yml -f compose.production.yaml)

if [[ ! -f .env ]]; then
  echo 'Create the VPS .env first. Keep the source APP_SECRET if restoring existing data.' >&2
  exit 2
fi
if ! command -v docker >/dev/null 2>&1; then
  echo 'Docker CLI was not found.' >&2
  exit 2
fi
"${compose[@]}" config --quiet

app_id="$("${compose[@]}" ps -q app 2>/dev/null || true)"
if [[ -n "$app_id" && "$(docker inspect --format '{{.State.Running}}' "$app_id")" == 'true' ]]; then
  echo 'Stop the app before restoring. The script will not restore into a running service.' >&2
  exit 1
fi

echo 'Starting only PostgreSQL...'
"${compose[@]}" up -d db
ready=false
for _ in $(seq 1 60); do
  if "${compose[@]}" exec -T db pg_isready -U foodshare -d foodshare >/dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 1
done
if [[ "$ready" != true ]]; then
  echo 'PostgreSQL did not become ready within 60 seconds.' >&2
  exit 1
fi

table_count="$("${compose[@]}" exec -T db psql -U foodshare -d foodshare -Atqc \
  "SELECT COUNT(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','S','f');")"
if [[ "$table_count" != '0' ]]; then
  echo "Target database is not empty ($table_count public relations). Nothing was restored." >&2
  echo 'Use a new empty VPS database/volume; do not run pg_restore --clean on live data.' >&2
  exit 1
fi

container_dump='/tmp/foodshare-migration.dump'
echo 'Copying and validating the database backup...'
"${compose[@]}" cp "$backup_dir/foodshare.dump" "db:$container_dump"
"${compose[@]}" exec -T db pg_restore --list "$container_dump" >/dev/null
echo 'Restoring into the empty database in one transaction...'
"${compose[@]}" exec -T db pg_restore --exit-on-error --single-transaction \
  --no-owner --no-privileges -U foodshare -d foodshare "$container_dump"
"${compose[@]}" exec -T db rm -f "$container_dump"

echo 'Building the app image and creating its container to initialize the persistent image volume...'
"${compose[@]}" build app
"${compose[@]}" create app
existing_upload="$("${compose[@]}" run --rm -T --no-deps --user 0 --entrypoint /bin/sh app \
  -c 'find /app/uploads -mindepth 1 -maxdepth 1 -print -quit')"
if [[ -n "$existing_upload" ]]; then
  echo 'Target image volume is not empty. Stop; no images were copied.' >&2
  exit 1
fi

echo 'Copying food images and assigning them to the app user...'
"${compose[@]}" cp "$backup_dir/uploads/." app:/app/uploads/
"${compose[@]}" run --rm -T --no-deps --user 0 --entrypoint /bin/sh app \
  -c 'chown -R 10001:10001 /app/uploads'

echo 'Starting the application and HTTPS proxy...'
"${compose[@]}" up -d app proxy
"${compose[@]}" ps
echo 'Restore finished. Check the public HTTPS URL at /actuator/health and test the app before sharing the URL.'
