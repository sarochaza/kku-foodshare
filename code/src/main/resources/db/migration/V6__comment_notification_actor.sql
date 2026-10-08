ALTER TABLE notifications ADD COLUMN IF NOT EXISTS actor_user_id BIGINT REFERENCES users(id);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS actor_name VARCHAR(120);
CREATE INDEX IF NOT EXISTS notifications_actor_user_idx ON notifications(actor_user_id);
