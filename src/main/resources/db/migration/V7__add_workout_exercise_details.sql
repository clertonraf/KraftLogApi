-- Add columns to workout_exercises to support exercise-specific details
ALTER TABLE workout_exercises ADD COLUMN recommended_sets INTEGER;
ALTER TABLE workout_exercises ADD COLUMN recommended_reps INTEGER;
ALTER TABLE workout_exercises ADD COLUMN training_technique VARCHAR(100);
ALTER TABLE workout_exercises ADD COLUMN order_index INTEGER;
