-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    birth_date DATE,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    weight_kg DOUBLE PRECISION,
    height_cm DOUBLE PRECISION,
    fitness_goal VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create muscles table
CREATE TABLE muscles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL,
    muscle_group VARCHAR(50) NOT NULL
);

-- Create exercises table
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sets INTEGER,
    repetitions INTEGER,
    technique VARCHAR(1000),
    default_weight_kg DOUBLE PRECISION,
    equipment_type VARCHAR(50)
);

-- Create exercise_muscles join table
CREATE TABLE exercise_muscles (
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    muscle_id UUID NOT NULL REFERENCES muscles(id) ON DELETE CASCADE,
    PRIMARY KEY (exercise_id, muscle_id)
);

-- Create routines table
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Create workouts table
CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    order_index INTEGER,
    interval_minutes INTEGER,
    routine_id UUID NOT NULL REFERENCES routines(id) ON DELETE CASCADE
);

-- Create workout_exercises join table
CREATE TABLE workout_exercises (
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    PRIMARY KEY (workout_id, exercise_id)
);

-- Create workout_muscles join table
CREATE TABLE workout_muscles (
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    muscle_id UUID NOT NULL REFERENCES muscles(id) ON DELETE CASCADE,
    PRIMARY KEY (workout_id, muscle_id)
);

-- Create aerobic_activities table
CREATE TABLE aerobic_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    duration_minutes INTEGER,
    notes VARCHAR(1000),
    routine_id UUID NOT NULL REFERENCES routines(id) ON DELETE CASCADE
);

-- Create log_routines table
CREATE TABLE log_routines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    routine_id UUID NOT NULL REFERENCES routines(id) ON DELETE CASCADE,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP
);

-- Create log_workouts table
CREATE TABLE log_workouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_routine_id UUID NOT NULL REFERENCES log_routines(id) ON DELETE CASCADE,
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP
);

-- Create log_exercises table
CREATE TABLE log_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_workout_id UUID NOT NULL REFERENCES log_workouts(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    start_datetime TIMESTAMP,
    end_datetime TIMESTAMP,
    notes VARCHAR(1000),
    repetitions INTEGER,
    completed BOOLEAN
);

-- Create log_sets table
CREATE TABLE log_sets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_exercise_id UUID NOT NULL REFERENCES log_exercises(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    reps INTEGER,
    weight_kg DOUBLE PRECISION,
    rest_time_seconds INTEGER,
    timestamp TIMESTAMP,
    notes VARCHAR(1000)
);

-- Create indexes for better query performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_routines_user_id ON routines(user_id);
CREATE INDEX idx_routines_is_active ON routines(is_active);
CREATE INDEX idx_workouts_routine_id ON workouts(routine_id);
CREATE INDEX idx_aerobic_activities_routine_id ON aerobic_activities(routine_id);
CREATE INDEX idx_log_routines_routine_id ON log_routines(routine_id);
CREATE INDEX idx_log_routines_start_datetime ON log_routines(start_datetime);
CREATE INDEX idx_log_workouts_log_routine_id ON log_workouts(log_routine_id);
CREATE INDEX idx_log_workouts_workout_id ON log_workouts(workout_id);
CREATE INDEX idx_log_exercises_log_workout_id ON log_exercises(log_workout_id);
CREATE INDEX idx_log_exercises_exercise_id ON log_exercises(exercise_id);
CREATE INDEX idx_log_sets_log_exercise_id ON log_sets(log_exercise_id);
