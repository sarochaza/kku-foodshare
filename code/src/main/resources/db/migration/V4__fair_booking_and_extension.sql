ALTER TABLE food_posts ADD COLUMN max_per_person INTEGER;

ALTER TABLE food_posts ADD CONSTRAINT post_max_per_person_valid
  CHECK (max_per_person IS NULL OR (max_per_person >= 1 AND max_per_person <= quantity));
