-- Add unique constraint to exercise name
-- Remove duplicate exercises keeping the one with most data (most recent)

-- First, identify and remove duplicates, keeping only the most complete record
DELETE FROM exercises
WHERE id NOT IN (
    SELECT MIN(id)
    FROM exercises
    GROUP BY name
);

-- Add unique constraint
ALTER TABLE exercises ADD CONSTRAINT uk_exercise_name UNIQUE (name);
