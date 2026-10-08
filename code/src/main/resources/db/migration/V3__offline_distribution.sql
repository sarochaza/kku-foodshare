-- Existing posts start with zero offline distributions. Preserve all rows, reservations and QR codes.
ALTER TABLE food_posts ADD COLUMN offline_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE food_posts DROP CONSTRAINT post_quantity;
ALTER TABLE food_posts ADD CONSTRAINT post_quantity CHECK (
    quantity > 0 AND reserved_quantity >= 0 AND collected_quantity >= 0 AND offline_quantity >= 0
    AND reserved_quantity + collected_quantity + offline_quantity <= quantity
);
