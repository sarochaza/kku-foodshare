-- Existing accounts keep their current experience; only future accounts start the guide.
ALTER TABLE users ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ALTER COLUMN onboarding_completed SET DEFAULT FALSE;
