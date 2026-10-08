-- Existing comments remain top-level rows; no rows or message text are removed.
ALTER TABLE post_comments ADD COLUMN parent_comment_id BIGINT REFERENCES post_comments(id);
ALTER TABLE post_comments ADD COLUMN reply_to_comment_id BIGINT REFERENCES post_comments(id);
CREATE INDEX post_comments_parent_idx ON post_comments(parent_comment_id, created_at, id);
