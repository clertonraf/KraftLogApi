# PDF Import Feature Migration

## Overview

The PDF import feature has been separated from the KraftLogApi into its own standalone service called **KraftLogPDFImport**.

## Why the Separation?

1. **Separation of Concerns**: Import functionality is distinct from the core API
2. **Independent Scaling**: Can scale the import service separately based on demand
3. **Easier Maintenance**: Simpler to maintain and update each service independently
4. **Reduced Dependencies**: Main API doesn't need PDF parsing libraries
5. **Better Architecture**: Follows microservices principles

## What Changed?

### Removed from KraftLogApi

The following components have been moved to KraftLogPDFImport:

- `ExerciseImportController` (POST `/api/admin/exercises/import-pdf`)
- `ExerciseImportService`
- `PdfExerciseParserService`
- `ExerciseImportProperties`
- PDF parsing dependencies (Apache PDFBox)

### Remains in KraftLogApi

The core exercise management functionality remains unchanged:

- `ExerciseController` - All CRUD operations for exercises
- `ExerciseService`
- `ExerciseRepository`
- Authentication and authorization
- All other API endpoints

## Migration Guide

### For Developers

If you were using the PDF import feature directly:

**Before:**
```bash
curl -X POST http://localhost:8080/api/admin/exercises/import-pdf \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@exercises.pdf"
```

**After:**
```bash
# No authentication needed - handled by KraftLogPDFImport
curl -X POST http://localhost:8081/api/import/pdf \
  -F "file=@exercises.pdf"
```

### Setting Up the New Service

1. Clone the KraftLogPDFImport repository
2. Configure connection to KraftLogApi:
   ```bash
   export KRAFTLOG_API_URL=http://localhost:8080
   export KRAFTLOG_API_USERNAME=admin
   export KRAFTLOG_API_PASSWORD=your_password
   ```
3. Run the service:
   ```bash
   cd KraftLogPDFImport
   mvn spring-boot:run
   ```

### Docker Deployment

Both services can run together using Docker Compose. See the KraftLogPDFImport repository for the complete `docker-compose.yml` configuration.

## Benefits

### For KraftLogApi

- **Lighter**: Removed PDF parsing dependencies
- **Faster startup**: Less code to load
- **Simpler**: Focused on core API functionality
- **Better security**: Import functionality isolated

### For KraftLogPDFImport

- **Dedicated service**: Optimized for PDF processing
- **Independent deployment**: Can be deployed separately
- **Easy to extend**: Add new import formats without affecting the main API
- **Handles authentication**: Manages its own connection to KraftLogApi

## Architecture

```
┌─────────────────────┐
│   Client/Browser    │
└──────────┬──────────┘
           │
           ├─────────────────────┐
           │                     │
           ▼                     ▼
┌─────────────────┐   ┌──────────────────┐
│  KraftLogApi    │◄──│ KraftLogPDFImport│
│  (Port 8080)    │   │  (Port 8081)     │
└────────┬────────┘   └──────────────────┘
         │
         ▼
  ┌─────────────┐
  │  PostgreSQL │
  └─────────────┘
```

## Communication Flow

1. User uploads PDF to KraftLogPDFImport
2. KraftLogPDFImport parses the PDF
3. KraftLogPDFImport authenticates with KraftLogApi
4. For each exercise, KraftLogPDFImport calls KraftLogApi's `POST /api/exercises`
5. KraftLogApi creates exercises in the database
6. Results are returned to the user

## Configuration

### KraftLogApi

No special configuration needed. Just ensure the Exercise endpoints are accessible and authentication is working.

### KraftLogPDFImport

Required environment variables:

```bash
KRAFTLOG_API_URL=http://localhost:8080
KRAFTLOG_API_USERNAME=admin
KRAFTLOG_API_PASSWORD=admin
EXERCISE_MUSCLE_GROUPS_CONFIG_PATH=exercise-muscle-groups.yml
```

## Testing

### Test KraftLogApi
```bash
# Verify API is running
curl http://localhost:8080/api/exercises

# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### Test KraftLogPDFImport
```bash
# Health check
curl http://localhost:8081/api/import/health

# Import PDF
curl -X POST http://localhost:8081/api/import/pdf \
  -F "file=@exercises.pdf"
```

## Rollback Plan

If you need to revert to the old architecture:

1. The old code is available in git history (before this migration)
2. Check out the commit before the separation
3. Rebuild and deploy the monolithic version

## Future Enhancements

With this separation, we can now:

- Add support for other import formats (CSV, Excel, JSON)
- Implement batch processing for large imports
- Add import scheduling
- Create a dedicated UI for import operations
- Scale import service independently during heavy usage

## Questions?

See the README files in both repositories for detailed information:

- `KraftLogApi/README.md` - Main API documentation
- `KraftLogPDFImport/README.md` - Import service documentation
- `KraftLogPDFImport/QUICK_START.md` - Quick setup guide

## Repository Links

- **KraftLogApi**: Main API repository
- **KraftLogPDFImport**: `/Users/clerton/workspace/KraftLogPDFImport`

## Version Information

- **KraftLogApi**: v1.1.0+ (after separation)
- **KraftLogPDFImport**: v1.0.0 (initial release)

---

Last updated: 2025-12-22
