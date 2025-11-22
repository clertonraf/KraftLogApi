# KraftLog API - Quick Start Guide

## 🚀 Starting the Application

### 1. Start PostgreSQL
```bash
cd /Users/clerton/workspace/KraftLogApi
docker-compose up -d postgres
```

### 2. Start Spring Boot API
```bash
cd /Users/clerton/workspace/KraftLogApi
bash -c 'source ~/.sdkman/bin/sdkman-init.sh && sdk use java 17.0.13-tem && mvn spring-boot:run'
```

**API will be available at:** `http://localhost:8080/api`

### 3. Default Admin Credentials
- **Email:** `admin@kraftlog.com`
- **Password:** `admin123`

---

## 🧹 Database Cleanup

### Quick Reset (Recommended)
Deletes all data but keeps the schema:
```bash
cd /Users/clerton/workspace/KraftLogApi
./reset-db.sh
```

### Alternative Scripts
- `reset-database.sh` - Requires psql installed locally
- `clean-database.sh` - Full database recreation with Flyway
- `clean-database.sql` - Pure SQL for manual execution

See `DATABASE_CLEANUP_README.md` for detailed documentation.

---

## 🛑 Stopping the Application

```bash
# Stop Spring Boot
pkill -f "spring-boot:run"

# Stop PostgreSQL
docker-compose stop postgres

# Stop everything
docker-compose down
```

---

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

### Users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Exercises
- `GET /api/exercises` - List all exercises
- `GET /api/exercises/{id}` - Get exercise details
- `POST /api/exercises` - Create exercise (admin only)
- `PUT /api/exercises/{id}` - Update exercise (admin only)
- `DELETE /api/exercises/{id}` - Delete exercise (admin only)

### Routines
- `GET /api/routines` - List all routines
- `GET /api/routines/user/{userId}` - Get user's routines
- `POST /api/routines` - Create routine
- `PUT /api/routines/{id}` - Update routine
- `DELETE /api/routines/{id}` - Delete routine

### Workout Logs
- `POST /api/log-routines` - Start workout session
- `GET /api/log-routines` - Get workout history
- `POST /api/log-sets` - Log exercise set
- `GET /api/log-sets/exercise/{exerciseId}` - Get set history

---

## 🔍 Swagger Documentation

When the API is running, visit:
**http://localhost:8080/swagger-ui.html**

---

## ⚙️ Configuration

### Change API Port
Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8080  # Change this
```

### Database Connection
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/kraftlog
    username: postgres
    password: postgres
```

---

## 🐛 Troubleshooting

### Port 8080 already in use
```bash
lsof -ti:8080 | xargs kill -9
```

### PostgreSQL connection refused
```bash
docker-compose restart postgres
# Wait 5 seconds, then restart Spring Boot
```

### Lombok errors during compilation
Make sure you're using Java 17:
```bash
bash -c 'source ~/.sdkman/bin/sdkman-init.sh && sdk use java 17.0.13-tem && java -version'
```

### CORS errors from frontend
CORS is enabled for all origins. Check if API is actually running:
```bash
curl http://localhost:8080/api/muscles
```

---

## 📦 Project Structure

```
KraftLogApi/
├── src/main/java/com/kraftlog/
│   ├── controller/      # REST endpoints
│   ├── service/         # Business logic
│   ├── repository/      # Database access
│   ├── entity/          # Database models
│   ├── dto/             # Data transfer objects
│   ├── security/        # JWT & authentication
│   ├── config/          # Spring configuration
│   └── exception/       # Error handling
├── src/main/resources/
│   ├── application.yml  # Configuration
│   └── db/migration/    # Flyway SQL migrations
├── docker-compose.yml   # PostgreSQL setup
├── reset-db.sh          # Quick database reset
└── pom.xml              # Maven dependencies
```

---

## 🎯 Next Steps

1. ✅ API is running on port 8080
2. ✅ Frontend can connect with CORS enabled
3. ✅ Database cleanup scripts available
4. 📱 Test with the Expo frontend
5. 🚀 Start building features!

**Happy coding! 💪**
