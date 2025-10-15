# KraftLog - Gym Exercise Logging API

A comprehensive REST API built with Spring Boot for tracking gym exercises, routines, and workout progress with JWT authentication.

## Features

- JWT-based authentication and authorization
- **Administrator role with privileged access**
- User management with profile information and fitness goals
- Exercise library with muscle group associations
- Workout routines with customizable exercises
- Aerobic activity tracking
- Detailed workout logging with sets, reps, and weights
- Progress tracking over time
- PostgreSQL database with UUID primary keys
- Flyway database migrations
- Complete API documentation with Swagger/OpenAPI

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security 6.2.1** - JWT Authentication
- **Spring MVC** - REST API
- **Spring Data JPA** - Data persistence
- **PostgreSQL 16** - Database
- **Flyway** - Database migrations
- **SpringDoc OpenAPI** - API documentation
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

## Data Model

### Core Entities
- **User**: User profile with fitness goals and authentication
- **Muscle**: Predefined muscle groups (Chest, Back, Legs, etc.)
- **Exercise**: Exercise templates with default parameters
- **Routine**: Workout plan for a user
- **Workout**: Collection of exercises within a routine
- **AerobicActivity**: Cardio activities within a routine

### Logging Entities
- **LogRoutine**: Tracks when a routine was performed
- **LogWorkout**: Tracks individual workout sessions
- **LogExercise**: Tracks exercise performance
- **LogSet**: Tracks individual sets with reps and weight

## Quick Start with Docker Compose (Recommended)

### Prerequisites
- Docker
- Docker Compose

### Run the Application

1. **Clone the repository**
```bash
git clone <repository-url>
cd KraftLog
```

2. **Start the application**
```bash
docker-compose up -d
```

This will:
- Start a PostgreSQL database container
- Build and start the application container
- Run database migrations automatically
- Expose the API on port 8080

3. **Access the application**
- API Base URL: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

4. **Stop the application**
```bash
docker-compose down
```

5. **Stop and remove volumes (reset database)**
```bash
docker-compose down -v
```

## Docker Images

Pre-built Docker images are automatically published to GitHub Container Registry on every release.

### Available Tags

- `ghcr.io/clertonraf/kraftlogapi:latest` - Latest stable release from main branch
- `ghcr.io/clertonraf/kraftlogapi:1.0.0` - Specific version
- `ghcr.io/clertonraf/kraftlogapi:1.0` - Major.minor version
- `ghcr.io/clertonraf/kraftlogapi:1` - Major version
- `ghcr.io/clertonraf/kraftlogapi:main` - Latest build from main branch
- `ghcr.io/clertonraf/kraftlogapi:main-<sha>` - Specific commit from main

### Using Pre-built Images

**Pull the latest image:**
```bash
docker pull ghcr.io/clertonraf/kraftlogapi:latest
```

**Run with Docker:**
```bash
docker run -d \
  --name kraftlog-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-postgres-host:5432/kraftlog \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=your-secure-password \
  -e ADMIN_EMAIL=admin@yourdomain.com \
  ghcr.io/clertonraf/kraftlogapi:latest
```

**Using with Docker Compose:**

Update your `docker-compose.yml` to use the pre-built image:
```yaml
services:
  app:
    image: ghcr.io/clertonraf/kraftlogapi:1.0.0
    container_name: kraftlog-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/kraftlog
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SERVER_PORT: 8080
      ADMIN_USERNAME: ${ADMIN_USERNAME:-admin}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD:-admin123}
      ADMIN_EMAIL: ${ADMIN_EMAIL:-admin@kraftlog.com}
    ports:
      - "8080:8080"
```

### Multi-Architecture Support

Images are built for multiple architectures:
- `linux/amd64` (x86_64)
- `linux/arm64` (ARM 64-bit, including Apple Silicon)

Docker will automatically pull the correct image for your platform.

### Image Features

- **Security**: Runs as non-root user `kraftlog`
- **Health Checks**: Built-in health check endpoint monitoring
- **Optimized**: Multi-stage build for minimal image size
- **Metadata**: OCI-compliant labels for better discoverability
- **Provenance**: Build attestation for supply chain security

### Versioning

This project follows [Semantic Versioning](https://semver.org/):
- **Major version** (1.x.x): Breaking changes
- **Minor version** (x.1.x): New features, backward compatible
- **Patch version** (x.x.1): Bug fixes, backward compatible

## Manual Setup (Development)

### Prerequisites
- Java 17 or higher
- Maven 3.9+
- PostgreSQL 14+

### Steps

1. **Clone the repository**
```bash
git clone <repository-url>
cd KraftLog
```

2. **Configure PostgreSQL**

Create a database:
```sql
CREATE DATABASE kraftlog;
```

3. **Update application.yml** (Optional)

Edit `src/main/resources/application.yml` with your database credentials if different:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kraftlog
    username: postgres
    password: postgres
```

4. **Build the project**
```bash
mvn clean install
```

5. **Run the application**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

The API documentation is available via Swagger UI when the application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token

### User Management
- `POST /api/users` - Create a user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Muscle Management
- `GET /api/muscles` - Get all muscles (pre-populated)

### Exercise Management
- `POST /api/exercises` - Create exercise (Admin only)
- `GET /api/exercises` - Get all exercises
- `GET /api/exercises/{id}` - Get exercise by ID
- `PUT /api/exercises/{id}` - Update exercise (Admin only)
- `DELETE /api/exercises/{id}` - Delete exercise (Admin only)

### Admin Management (Admin only)
- `DELETE /api/admin/users/{userId}` - Delete any user
- `PUT /api/admin/users/{userId}/password` - Change any user's password

### Routine Management
- `POST /api/routines` - Create routine
- `GET /api/routines` - Get all routines
- `GET /api/routines/{id}` - Get routine by ID
- `GET /api/routines/user/{userId}` - Get routines by user
- `PUT /api/routines/{id}` - Update routine
- `DELETE /api/routines/{id}` - Delete routine

### Routine Logging
- `POST /api/log-routines` - Start a routine session
- `GET /api/log-routines` - Get all routine sessions
- `GET /api/log-routines/{id}` - Get routine session by ID
- `PUT /api/log-routines/{id}` - Update routine session
- `DELETE /api/log-routines/{id}` - Delete routine session

### Set Logging
- `POST /api/log-sets` - Log a set
- `GET /api/log-sets` - Get all logged sets
- `GET /api/log-sets/{id}` - Get logged set by ID
- `GET /api/log-sets/log-exercise/{logExerciseId}` - Get sets by exercise
- `PUT /api/log-sets/{id}` - Update logged set
- `DELETE /api/log-sets/{id}` - Delete logged set

## Authentication

All endpoints (except `/api/auth/**`, `/swagger-ui/**`, and `/v3/api-docs/**`) require JWT authentication.

### How to Authenticate

1. **Register a new user**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "password": "securepassword",
    "birthDate": "1990-01-15",
    "weightKg": 80.0,
    "heightCm": 180.0,
    "fitnessGoal": "MUSCLE_GAIN"
  }'
```

2. **Login to get JWT token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securepassword"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": "...",
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com"
  }
}
```

3. **Use the token in subsequent requests**
```bash
curl -X GET http://localhost:8080/api/exercises \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Complete Workflow Example

### 1. Register and Login
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "password": "password123",
    "birthDate": "1990-01-15",
    "weightKg": 80.0,
    "heightCm": 180.0,
    "fitnessGoal": "MUSCLE_GAIN"
  }'

# Login and save token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "password123"}' \
  | jq -r '.token')
```

### 2. Create Exercises
```bash
# Get available muscles
curl -X GET http://localhost:8080/api/muscles \
  -H "Authorization: Bearer $TOKEN"

# Create a Bench Press exercise
EXERCISE_ID=$(curl -X POST http://localhost:8080/api/exercises \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Barbell Bench Press",
    "description": "Classic chest exercise",
    "sets": 4,
    "repetitions": 10,
    "defaultWeightKg": 80.0,
    "equipmentType": "BARBELL",
    "muscleIds": ["CHEST_MUSCLE_UUID"]
  }' | jq -r '.id')
```

### 3. Create a Routine
```bash
ROUTINE_ID=$(curl -X POST http://localhost:8080/api/routines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Chest Day",
    "startDate": "2025-01-01",
    "isActive": true,
    "userId": "YOUR_USER_ID"
  }' | jq -r '.id')
```

### 4. Start a Workout Session
```bash
LOG_ROUTINE_ID=$(curl -X POST http://localhost:8080/api/log-routines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "routineId": "'$ROUTINE_ID'",
    "startDatetime": "2025-10-14T10:00:00"
  }' | jq -r '.id')
```

### 5. Log Exercise Sets
```bash
# Log first set
curl -X POST http://localhost:8080/api/log-sets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "logExerciseId": "LOG_EXERCISE_ID",
    "setNumber": 1,
    "reps": 10,
    "weightKg": 80.0,
    "restTimeSeconds": 90
  }'
```

## Fitness Goals

Available fitness goals:
- `WEIGHT_LOSS` - Focus on losing weight
- `MUSCLE_GAIN` - Focus on building muscle
- `MAINTENANCE` - Maintain current fitness level
- `STRENGTH` - Focus on increasing strength
- `ENDURANCE` - Focus on improving endurance

## Muscle Groups

Pre-configured muscle groups:
- CHEST
- DELTOIDS
- SHOULDERS
- BICEPS
- TRICEPS
- BACK
- FOREARMS
- GLUTES
- LEGS
- CALVES

## Equipment Types

Supported equipment:
- BARBELL
- DUMBBELL
- MACHINE
- SMITH_MACHINE
- CABLE
- BODYWEIGHT
- KETTLEBELL
- RESISTANCE_BAND
- OTHER

## Metric System

All measurements use the metric system:
- Weight: Kilograms (kg)
- Height: Centimeters (cm)
- Distance: Kilometers (km)

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:
- `V1__create_initial_schema.sql` - Creates all tables
- `V2__insert_muscle_groups.sql` - Seeds predefined muscle groups
- `V3__add_admin_role.sql` - Adds admin role column to users table

Migrations run automatically on application startup.

## Testing

Run all tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## Project Structure

```
src/main/java/com/kraftlog/
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
├── service/         # Business logic layer
├── controller/      # REST controllers
├── dto/             # Data transfer objects
├── config/          # Configuration classes
├── security/        # Security configuration & JWT
└── exception/       # Custom exceptions

src/main/resources/
├── db/migration/    # Flyway migration scripts
└── application.yml  # Application configuration

src/test/java/
└── com/kraftlog/
    ├── integration/ # Integration tests
    └── service/     # Unit tests
```

## Administrator Role

### Overview

The application includes an administrator role with special privileges:
- **Create, update, and delete exercises** in the database
- **Delete users** from the system
- **Change passwords** for any user

### Admin Initialization

An admin user is automatically created on first startup. You can customize admin credentials using environment variables.

### Configuring Admin Credentials

**Option 1: Using Environment Variables (Docker Compose)**

Create a `.env` file in the root directory:
```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-secure-password
ADMIN_EMAIL=admin@yourdomain.com
```

Then run:
```bash
docker-compose up -d
```

**Option 2: Set Environment Variables Directly**

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=your-secure-password
export ADMIN_EMAIL=admin@yourdomain.com
mvn spring-boot:run
```

**Option 3: Docker Compose Override**

Update `docker-compose.yml` environment section:
```yaml
environment:
  ADMIN_USERNAME: admin
  ADMIN_PASSWORD: your-secure-password
  ADMIN_EMAIL: admin@yourdomain.com
```

### Default Admin Credentials

If no environment variables are set, the default admin credentials are:
- **Email**: `admin@kraftlog.com`
- **Password**: `admin123`

**⚠️ Important**: Change the admin password immediately after first login for security!

### Admin Operations

**1. Login as Admin**
```bash
ADMIN_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@kraftlog.com",
    "password": "admin123"
  }' | jq -r '.token')
```

**2. Create an Exercise (Admin only)**
```bash
curl -X POST http://localhost:8080/api/exercises \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Squat",
    "description": "Compound leg exercise",
    "sets": 4,
    "repetitions": 8,
    "defaultWeightKg": 100.0,
    "equipmentType": "BARBELL",
    "muscleIds": ["LEG_MUSCLE_UUID"]
  }'
```

**3. Delete a User (Admin only)**
```bash
curl -X DELETE http://localhost:8080/api/admin/users/{userId} \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**4. Change User Password (Admin only)**
```bash
curl -X PUT http://localhost:8080/api/admin/users/{userId}/password \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword": "newSecurePassword123"
  }'
```

### Admin vs Regular Users

| Feature | Regular User | Admin User |
|---------|-------------|------------|
| View exercises | ✅ | ✅ |
| Create exercises | ❌ | ✅ |
| Update exercises | ❌ | ✅ |
| Delete exercises | ❌ | ✅ |
| Manage own profile | ✅ | ✅ |
| Delete other users | ❌ | ✅ |
| Change other users' passwords | ❌ | ✅ |
| Create routines | ✅ | ✅ |
| Log workouts | ✅ | ✅ |

## Environment Variables

The application can be configured using environment variables:

### Database Configuration
- `SPRING_DATASOURCE_URL` - Database URL (default: jdbc:postgresql://localhost:5432/kraftlog)
- `SPRING_DATASOURCE_USERNAME` - Database username (default: postgres)
- `SPRING_DATASOURCE_PASSWORD` - Database password (default: postgres)

### Server Configuration
- `SERVER_PORT` - Server port (default: 8080)

### JWT Configuration
- `JWT_SECRET` - JWT signing secret (default: provided in application.yml)
- `JWT_EXPIRATION` - JWT expiration in milliseconds (default: 86400000 - 24 hours)

### Admin Configuration
- `ADMIN_USERNAME` - Admin username (default: admin)
- `ADMIN_PASSWORD` - Admin password (default: admin123)
- `ADMIN_EMAIL` - Admin email (default: admin@kraftlog.com)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License
