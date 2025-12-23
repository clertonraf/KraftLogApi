-- Add unique constraint to exercise name
-- Remove duplicate exercises keeping one record per name

-- First, identify and remove duplicates, keeping only one record for each name
DELETE FROM exercises
WHERE id IN (
    SELECT id
    FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY name ORDER BY id) as row_num
        FROM exercises
    ) t
    WHERE t.row_num > 1
);

-- Add unique constraint
ALTER TABLE exercises ADD CONSTRAINT uk_exercise_name UNIQUE (name);
