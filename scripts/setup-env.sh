#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -e .env ]]; then echo '.env exists; leaving it unchanged.'; exit 0; fi
command -v openssl >/dev/null || { echo 'Install OpenSSL first.'; exit 1; }
umask 077
cp .env.example .env
sed -i.bak "s/^DATABASE_PASSWORD=$/DATABASE_PASSWORD=$(openssl rand -hex 24)/; s/^APP_SECRET=$/APP_SECRET=$(openssl rand -hex 32)/" .env
rm -f .env.bak
echo 'Created .env with random secrets. Run: docker compose -p kku-foodshare-phase1 -f docker-compose.yml up --build -d'
