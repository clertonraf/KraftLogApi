# Spring-Based Database Cleanup

## Overview

The KraftLog API now includes **native Spring Framework database cleanup functionality** using JPA EntityManager. This provides a programmatic way to clean the database through REST API endpoints.

## Components

### 1. `DatabaseCleaner` Component
Located at: `src/main/java/com/kraftlog/util/DatabaseCleaner.java`

**Methods:**
- `truncateAllTables()` - Fast truncation using SQL TRUNCATE
- `deleteAllEntities()` - Safer deletion using JPA entity manager
- `logTableCounts()` - Logs row counts for verification

**Features:**
- Uses `@Transactional(propagation = Propagation.REQUIRES_NEW)` for clean transactions
- Handles tables that may not exist
- Logs detailed operation results

### 2. Admin REST Endpoints
Located at: `src/main/java/com/kraftlog/controller/AdminController.java`

**Endpoints:**

#### POST `/api/admin/database/clean`
Truncates all tables using SQL TRUNCATE (fast method)

**Requirements:**
- Admin authentication (Bearer token)
- Admin role

**Example:**
```bash
curl -X POST http://localhost:8080/api/admin/database/clean \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "status": "success",
  "message": "Database cleaned successfully. All data has been deleted.",
  "note": "Restart the application to recreate the admin user."
}
```

#### POST `/api/admin/database/delete-entities`
Deletes all entities using JPA (safer but slower method)

**Requirements:**
- Admin authentication (Bearer token)
- Admin role

**Example:**
```bash
curl -X POST http://localhost:8080/api/admin/database/delete-entities \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Usage

### Method 1: Using the Test Script

```bash
cd /Users/clerton/workspace/KraftLogApi
./test-cleanup-api.sh
```

This script:
1. Logs in as admin
2. Gets JWT token
3. Calls the cleanup endpoint
4. Shows the result

### Method 2: Using Swagger UI

1. Start the application
2. Go to: **http://localhost:8080/swagger-ui.html**
3. Click "Authorize" and enter admin credentials:
   - Email: `admin@kraftlog.com`
   - Password: `admin123`
4. Navigate to "Admin Management" section
5. Try out `POST /api/admin/database/clean`

### Method 3: Using curl Manually

```bash
# Step 1: Login as admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@kraftlog.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*' | sed 's/"token":"//')

# Step 2: Clean database
curl -X POST http://localhost:8080/api/admin/database/clean \
  -H "Authorization: Bearer $TOKEN"
```

### Method 4: From Application Code

Inject the `DatabaseCleaner` bean into any Spring component:

```java
@Autowired
private DatabaseCleaner databaseCleaner;

public void someMethod() {
    // Clean all tables
    databaseCleaner.truncateAllTables();
    
    // OR use JPA delete (slower)
    databaseCleaner.deleteAllEntities();
}
```

---

## Comparison: Spring vs Shell Scripts

| Feature | Spring API | Shell Scripts |
|---------|-----------|---------------|
| **Access** | REST API, Swagger UI, Java code | Command line only |
| **Authentication** | JWT required | None (direct DB access) |
| **Authorization** | Admin role check | None |
| **Logging** | Integrated with application logs | Separate output |
| **Auditing** | Can be logged/tracked | Manual tracking |
| **Remote Access** | ✅ Yes (via HTTP) | ❌ No (local only) |
| **Integration Testing** | ✅ Easy | ❌ Harder |
| **Speed** | Fast | Fast |
| **Safety** | Transaction-based | Direct SQL |

---

## When to Use Each Method

### Use Spring API When:
- ✅ You need programmatic access
- ✅ Building automated tests
- ✅ Remote database cleanup needed
- ✅ Want audit trail of cleanups
- ✅ Need role-based access control
- ✅ Integrating with CI/CD pipelines

### Use Shell Scripts When:
- ✅ Quick local development cleanup
- ✅ No application running
- ✅ Direct database maintenance
- ✅ One-time manual operations

---

## Security Considerations

⚠️ **Important Security Notes:**

1. **Admin-Only Access**: Both endpoints require admin authentication
2. **Production Warning**: Consider disabling these endpoints in production:
   ```java
   @Profile("!production")
   @PostMapping("/database/clean")
   ```
3. **Audit Logging**: All cleanup operations are logged with admin's JWT
4. **No Undo**: Database cleanup is permanent!

---

## After Cleanup

After cleaning the database:

1. **Admin user is deleted** - Restart the app to recreate:
   ```bash
   pkill -f "spring-boot:run"
   cd /Users/clerton/workspace/KraftLogApi
   bash -c 'source ~/.sdkman/bin/sdkman-init.sh && sdk use java 17.0.13-tem && mvn spring-boot:run'
   ```

2. **Check logs** for table counts:
   ```bash
   tail -50 /tmp/kraftlog-app.log | grep '📊'
   ```

3. **Verify in Swagger** that tables are empty

---

## Monitoring

Database cleanup operations are logged with emoji for easy filtering:

- 🧹 = Cleanup started
- ✅ = Cleanup successful
- ❌ = Cleanup failed  
- 📊 = Table counts
- ✓ = Individual table truncated

**Example log output:**
```
🧹 Starting database cleanup - ALL DATA WILL BE DELETED!
✓ Truncated table: log_sets
✓ Truncated table: log_exercises
✓ Truncated table: users
✅ All tables truncated successfully!
📊 Current table counts:
  users = 0 rows
  exercises = 0 rows
```

---

## Testing

Test the cleanup functionality:

```bash
# Test truncate method
./test-cleanup-api.sh

# Verify tables are empty
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/database/status
```

---

## Integration with Test Suites

Example Spring Boot test:

```java
@SpringBootTest
@AutoConfigureMockMvc
class CleanupIntegrationTest {
    
    @Autowired
    private DatabaseCleaner databaseCleaner;
    
    @BeforeEach
    void setUp() {
        databaseCleaner.truncateAllTables();
    }
    
    @Test
    void testWithCleanDatabase() {
        // Your test here with empty database
    }
}
```

---

## Files Created

1. ✅ `src/main/java/com/kraftlog/util/DatabaseCleaner.java`
2. ✅ Updated `src/main/java/com/kraftlog/controller/AdminController.java`
3. ✅ `test-cleanup-api.sh` - Test script for API
4. ✅ This documentation file

---

## Summary

**You now have THREE ways to clean the database:**

1. 🚀 **Spring REST API** - Modern, secure, auditable (RECOMMENDED for production-like environments)
2. ⚡ **Shell Scripts** (`reset-db.sh`) - Fast local development (RECOMMENDED for dev)
3. 📝 **SQL Script** (`clean-database.sql`) - Manual database client execution

Choose the method that best fits your workflow! 💪
