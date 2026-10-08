# Deployment

## Existing cloud configuration (Render + Neon)

The uploaded phase notes name `https://kku-foodshare.onrender.com`; this package does not certify its current commit or availability. Keep the current DATABASE_URL/DATABASE_USER/DATABASE_PASSWORD, APP_SECRET and Google credentials when updating source. Do not replace the production database or secret while moving files.

Food image provider settings:

| Key | Value / meaning |
|---|---|
| IMAGE_STORAGE_PROVIDER | cloudinary to use cloud images, local for persistent local volume |
| CLOUDINARY_CLOUD_NAME | your cloud name |
| CLOUDINARY_API_KEY | your provider key |
| CLOUDINARY_API_SECRET | your provider secret |

Password-reset email provider settings:

| Key | Value / meaning |
|---|---|
| MAIL_ENABLED | true when mail is configured |
| MAIL_PROVIDER | brevo for HTTPS API; smtp for SMTP |
| BREVO_API_KEY | your API key, never committed |
| MAIL_FROM | verified sender |
| APP_BASE_URL | your actual public HTTPS URL |

Brevo does not require the SMTP mail profile. Google login uses the `google` profile and existing GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET. Authorized redirect URI must match the public hostname and `/login/oauth2/code/google`. SMTP uses the existing `mail` profile/settings instead of Brevo. Configure only providers you actually use.

After deploying the submitted commit, check health and Swagger, register/login, profile, create/edit a post with images/location, reserve/cancel/collect, owner stock, comments and reminders. Test upload persistence across redeploy and reset-email delivery using your accounts. Provider fixture tests do not prove real delivery. Public camera/GPS require HTTPS and permission.

## VPS / Docker alternative and backup/restore

The remainder preserves the existing optional VPS deployment and data migration instructions. Choose this only if the project uses a VPS instead of its existing Render/Neon configuration.

# Deployment and database operations

## Production layout

This project is prepared for one Spring Boot app, one private PostgreSQL 17 service, persistent Docker volumes for PostgreSQL and `/app/uploads`, and Caddy as the public HTTPS proxy. PostgreSQL and the app port are not exposed publicly; only Caddy ports 80/443 are published. The setup is intended for a low-traffic course project, not multiple app instances.

The course-required base file is `docker-compose.yml`. `compose.yaml` remains as a compatibility copy for commands in older phase notes; keep both base files equivalent. `compose.production.yaml` adds the public domain and secure-cookie settings.

## VPS preparation

Use a Linux VPS or a university server with Docker Compose, persistent disk, and enough memory for the app and PostgreSQL. The repository README suggests starting around 2 GB RAM for a small deployment. Before starting Caddy:

1. Point the domain's DNS A/AAAA records to the server.
2. Allow inbound TCP ports 80 and 443 in the provider firewall and server firewall.
3. Clone the GitHub repository to a stable directory such as `/opt/kku-foodshare`.
4. Run `bash scripts/setup-env.sh`, then edit `.env` on the server. Set `DOMAIN` to the hostname only (no `https://` or path). Keep `.env` off GitHub.
5. For a fresh database, keep the generated `APP_SECRET`. For existing data, replace it with the exact source `APP_SECRET` before restoring. The target `DATABASE_PASSWORD` may be new because the backup does not contain database roles.
6. Add Google OAuth and SMTP settings only if those features will be used. Update Google's authorized callback to `https://<DOMAIN>/login/oauth2/code/google`.

Fresh deployment with an empty database:

```bash
cd /opt/kku-foodshare
bash scripts/setup-env.sh
# Edit .env and set DOMAIN before continuing.
docker compose -p kku-foodshare-prod -f docker-compose.yml -f compose.production.yaml up --build -d
docker compose -p kku-foodshare-prod -f docker-compose.yml -f compose.production.yaml ps
```

Keep the project name `kku-foodshare-prod` for future updates. Compose uses it to name persistent volumes; changing it makes Docker create different, empty volumes. Do not run `docker compose down -v` when data must be kept.

## Move the current Windows database and uploaded images

The database inside Docker Desktop on the Windows laptop cannot be transferred by deploying the app alone. Export the database and image volume as a matching set. Run this during a quiet period when nobody is posting, uploading, or changing reservations. The export folder contains private user data; do not commit it or put it in the delivery ZIP.

Extract the deployment ZIP, then copy the exact `.env` file used by the currently running local project into the extracted `kku-foodshare` folder. This lets Compose connect to the existing project safely. Keep this secret file only on your computer; it is not part of the ZIP.

From PowerShell, in that `kku-foodshare` folder that contains `docker-compose.yml`, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\backup-local.ps1 -ProjectName kku-foodshare-phase1
```

The script writes a timestamped folder under `backups\` containing `foodshare.dump`, `uploads\`, and `BACKUP-INFO.txt`. It uses `docker compose cp` to copy the binary dump, so PowerShell does not re-encode it. It intentionally does not copy `.env` or secrets.

Copy that folder to the VPS over SSH. Replace the example folder name with the one printed by the backup script:

```powershell
scp -r .\backups\foodshare-20261007-230000-000 user@YOUR_SERVER:/opt/kku-foodshare/backups/
```

On the VPS, first prepare `.env` as described above and ensure `DOMAIN` and the source `APP_SECRET` are correct. Do not start the app yet. Then run the guarded restore script:

```bash
cd /opt/kku-foodshare
bash scripts/restore-vps.sh /opt/kku-foodshare/backups/foodshare-20261007-230000-000
```

The script starts only PostgreSQL, verifies the target database has no application relations, validates the dump, restores it in one transaction without `--clean`, creates the image volume, restores image ownership to UID/GID 10001, and starts the app and Caddy proxy. It stops if the database or image volume is already populated. Do not bypass that guard to overwrite existing data; inspect the target and take a separate backup first.

If the VPS should start with demo data only, skip the backup/restore steps and use the fresh deployment procedure instead. Flyway creates the schema on the first app start.

## Verify the public deployment

After Caddy has obtained the HTTPS certificate, check:

```bash
docker compose -p kku-foodshare-prod -f docker-compose.yml -f compose.production.yaml ps
curl -fsS https://YOUR_DOMAIN/actuator/health
```

The health endpoint should return `UP`. Then test signup/login, create a post with an image and location, reserve/cancel, QR/manual pickup, comments, and the map from a phone. Restart the app and confirm posts and images remain. If Google login is enabled, verify the OAuth callback using the public domain.

Before submitting the course work, replace the Deployment URL placeholder in `README.md` with the live URL and verify `https://YOUR_DOMAIN/swagger-ui/index.html` opens publicly.

## Ongoing backups

Back up PostgreSQL and uploads together before upgrades. Store a copy on a different machine or private storage, separately preserve `APP_SECRET`, and test restore on an empty staging stack. Never upload database dumps, uploaded user images, or `.env` to GitHub.

For PostgreSQL-only maintenance in a private environment:

```bash
docker compose -p kku-foodshare-prod -f docker-compose.yml -f compose.production.yaml exec -T db pg_dump -U foodshare -d foodshare -Fc -f /tmp/foodshare.dump
docker compose -p kku-foodshare-prod -f docker-compose.yml -f compose.production.yaml cp db:/tmp/foodshare.dump ./foodshare.dump
```

Do not edit Flyway migrations already applied to a database. Add a new migration for schema changes and test it against a restored copy before production.

## Troubleshooting

- Startup says `APP_SECRET`: set a random value of at least 32 characters in `.env`.
- Caddy does not issue HTTPS: verify DNS points to the VPS and ports 80/443 are reachable.
- Database connection fails: confirm the `db` service is healthy. Do not publish PostgreSQL port 5432.
- Images are missing after migration: confirm the `uploads/` folder was copied and the restore script completed before opening the proxy.
- Pickup codes cannot be read after migration: restore the exact source `APP_SECRET` and restart the app. Changing it can invalidate encrypted pickup data.
- Session requires a new login after app restart: sessions are in memory; account, posts, and reservations remain in PostgreSQL.
- Map tiles fail: verify outbound internet access and the configured tile provider. OpenStreetMap tiles require attribution and are best-effort for low-traffic demos.
- Email/reset-password does not send: configure an SMTP provider and test its sender/domain on the production URL.
