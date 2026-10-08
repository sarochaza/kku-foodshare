# VPS Deployment and Database Migration Implementation Plan

> **For agentic workers:** Execute inline task by task; keep the existing application behavior and database intact.

**Goal:** Prepare the current KKU FoodShare project for a public VPS deployment and provide a safe path to move the existing PostgreSQL data and uploaded images.

**Architecture:** Keep the existing Spring Boot, PostgreSQL, Docker Compose, and Caddy production stack. Add the exact Compose filename required by the course rubric, plus Windows-safe database/image export and guarded Linux restore instructions. Do not create a database dump in this workspace because the user's live database is on their Windows Docker host.

**Tech Stack:** Spring Boot 3, Java 17, PostgreSQL 17, Docker Compose, Caddy, PowerShell, Bash.

**Spec:** Course assignment PDF provided in this conversation; current `compose.yaml`, `compose.production.yaml`, `Caddyfile`, `.env.example`, and `doc/deployment.md`.

## Global Constraints

- Preserve the existing local Compose project name and volumes when backing up from Windows.
- Never include `.env`, database dumps, or user uploads in Git or the delivery ZIP.
- Preserve `APP_SECRET` when migrating existing reservation/pickup data.
- Restore only into a new, empty database; never use `pg_restore --clean` on an active database.
- Keep PostgreSQL private; expose the application only through Caddy on ports 80/443.

## Review Focus

- PowerShell must copy custom-format dumps as files rather than redirecting binary stdout.
- Restore must stop before modifying a target database that already contains application tables.
- Uploaded images must be restored into the persistent volume with owner UID/GID 10001.
- Compose filenames must remain equivalent so existing `compose.yaml` commands keep working.
- Secrets and backups must be excluded from both Git and Docker build context.

---

### Task 1: Course-Compatible Compose Entry Point

**Files:**
- Create: `docker-compose.yml` matching `compose.yaml`
- Modify: `.gitignore`, `.dockerignore`

- [x] Add the course-required `docker-compose.yml` base configuration while retaining `compose.yaml` for compatibility.
- [x] Exclude `backups/` and `*.dump` from Git and Docker build context.
- [x] Verify both Compose YAML files parse to equivalent service/volume definitions.

### Task 2: Database and Upload Migration

**Files:**
- Create: `scripts/backup-local.ps1`
- Create: `scripts/restore-vps.sh`

- [x] Add a PowerShell exporter for a PostgreSQL custom-format dump from the local `db` container into a timestamped ignored backup folder.
- [x] Copy `/app/uploads` from the running app container into the same backup folder; do not copy `.env`.
- [x] Restore only to an empty target DB, validate the dump before import, and use a single transaction with no destructive clean flags.
- [x] Restore images into the app volume and set owner to UID/GID 10001 before starting the public proxy.
- [x] Run `bash -n` on the restore script; PowerShell cannot be executed in this workspace, so the script remains to be run on the user's Windows host.

### Task 3: Deployment Instructions

**Files:**
- Modify: `doc/deployment.md`, `README.md`

- [x] Document VPS setup, stable Compose project name, DNS/HTTPS, secret handling, optional fresh DB vs data migration, and public smoke tests.
- [x] Document Windows PowerShell export and `scp` transfer steps, plus Linux VPS restore invocation.
- [x] Add a clearly marked placeholder for the actual public Deployment URL and Swagger URL.
- [x] Validate Compose YAML, script syntax, ignored paths, and links/commands in the changed docs.

## Decisions and verification

- Ruling: Keep `compose.yaml` and add the rubric-named `docker-compose.yml` as an equivalent base file — this satisfies the course's filename check while preserving commands in earlier phase notes; the cost is keeping the two base files synchronized.
- Ruling: Do not create a dump in this workspace — the user's live PostgreSQL volume is on the Windows Docker host; exporting it here is impossible, so the included PowerShell script creates the real backup there.
- Verified: YAML parsing and base-file equivalence passed; restore script passed `bash -n`; repository/build ignore rules include backup folders and `.dump` files.
- Not verified here: Docker Compose runtime and PowerShell execution; Docker CLI and PowerShell are not installed in this workspace.
