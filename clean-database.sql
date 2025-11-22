-- KraftLog Database Cleanup SQL Script
-- Execute this directly in psql or pgAdmin
-- Usage: psql -h localhost -p 5433 -U postgres -d kraftlog -f clean-database.sql

-- Disable foreign key checks temporarily
SET session_replication_role = 'replica';

-- Truncate all data tables (keeps structure)
TRUNCATE TABLE log_sets CASCADE;
TRUNCATE TABLE log_exercises CASCADE;
TRUNCATE TABLE log_routines CASCADE;
TRUNCATE TABLE routine_exercises CASCADE;
TRUNCATE TABLE routines CASCADE;
TRUNCATE TABLE exercise_muscles CASCADE;
TRUNCATE TABLE exercises CASCADE;
TRUNCATE TABLE muscles CASCADE;
TRUNCATE TABLE users CASCADE;

-- Re-enable foreign key checks
SET session_replication_role = 'origin';

-- Show confirmation
SELECT 'Database cleaned successfully!' as status;

-- Show table counts (should all be 0)
SELECT 
    'users' as table_name, 
    COUNT(*) as row_count 
FROM users
UNION ALL
SELECT 'muscles', COUNT(*) FROM muscles
UNION ALL
SELECT 'exercises', COUNT(*) FROM exercises
UNION ALL
SELECT 'routines', COUNT(*) FROM routines
UNION ALL
SELECT 'log_routines', COUNT(*) FROM log_routines
UNION ALL
SELECT 'log_exercises', COUNT(*) FROM log_exercises
UNION ALL
SELECT 'log_sets', COUNT(*) FROM log_sets;
