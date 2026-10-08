-- The original quick-place coordinate was south of the Central Library building.
-- Correct only that exact legacy suggestion; manually selected post locations stay untouched.
UPDATE food_posts
SET latitude = 16.4768000,
    longitude = 102.8232500
WHERE latitude = 16.4756000
  AND longitude = 102.8228000
  AND pickup_location_name = 'หอสมุดกลาง มหาวิทยาลัยขอนแก่น';
