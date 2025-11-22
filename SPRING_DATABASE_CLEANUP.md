# Database Cleanup - Exercises Removed

## ✅ Completed Successfully

**Date**: November 22, 2025  
**Time**: 15:57 UTC

## What Was Cleaned

All exercises and related associations have been removed:

```
✅ exercises          → 0 rows
✅ exercise_muscles   → 0 rows  
✅ workout_exercises  → 0 rows
✅ log_exercises      → 0 rows
```

## What Was Preserved

- ✅ Database schema and structure
- ✅ Users (including admin)
- ✅ Muscles (10 muscle groups)
- ✅ Routines
- ✅ Workouts
- ✅ All migrations

## Next Steps

### Start the Application

With the fixed PDF parser:

```bash
export EXERCISE_MUSCLE_GROUPS_CONFIG_PATH=$(pwd)/exercise-muscle-groups.yml
mvn spring-boot:run
```

### Import Exercises (Clean Names!)

```bash
# Login as admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@kraftlog.com","password":"admin123"}' \
  | jq -r '.token')

# Import exercises from PDF (with fixed parser)
curl -X POST http://localhost:8080/api/admin/exercises/import-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@tmp/lista-de-videos-de-exercicios.pdf"
```

**Expected Result**:
- ✅ ~257 exercises imported
- ✅ Exercise names WITHOUT URLs
- ✅ Video URLs stored separately
- ✅ Muscle group associations

### Verify Import

```bash
# Get first exercise
curl -s http://localhost:8080/api/exercises \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.[0]'

# Should show:
{
  "id": "...",
  "name": "Supino Reto Barra",          # Clean name!
  "videoUrl": "https://youtu.be/...",   # Separate URL!
  "muscles": [...],
  "sets": null,
  "repetitions": null
}
```

## Quick Commands

### Check exercise count
```bash
docker exec kraftlog-postgres psql -U postgres -d kraftlog -c \
  "SELECT COUNT(*) FROM exercises;"
```

### List exercises with URLs
```bash
docker exec kraftlog-postgres psql -U postgres -d kraftlog -c \
  "SELECT name, video_url FROM exercises LIMIT 10;"
```

### Clean exercises again (if needed)
```bash
docker exec kraftlog-postgres psql -U postgres -d kraftlog -c "
TRUNCATE TABLE exercises CASCADE;
"
```

## Notes

- ✅ Unique constraint on exercise name is active
- ✅ Upsert behavior enabled (re-import is safe)
- ✅ PDF parser fixed (no more URL concatenation)
- ✅ External muscle group configuration loaded

---

**Status**: Ready for fresh import with clean data ✅
