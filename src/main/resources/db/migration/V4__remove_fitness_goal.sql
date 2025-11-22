-- Remove fitness_goal column from users table
ALTER TABLE users DROP COLUMN IF EXISTS fitness_goal;
