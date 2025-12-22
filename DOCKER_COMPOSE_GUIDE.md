# Docker Compose Usage Guide

This project includes two Docker Compose configurations:
- `docker-compose.dev.yml` - For development
- `docker-compose.prod.yml` - For production

Both configurations include:
- **KraftLog API** - Main REST API (port 8080)
- **KraftLog PDF Import** - PDF import service (port 8081)
- **PostgreSQL** - Database

## Development Setup

### Quick Start
```bash
# Start development environment (all services)
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f app
docker-compose -f docker-compose.dev.yml logs -f pdf-import

# Stop
docker-compose -f docker-compose.dev.yml down
```

### Features
- **Builds locally**: Uses Dockerfile to build from source
- **Verbose logging**: DEBUG level for application code
- **Development database**: PostgreSQL on port 5433
- **Default credentials**: admin/admin123
- **Hot reload**: Source code mounted as volume (requires restart)
- **PDF Import included**: Automatically configured to connect to API

### Configuration
Development uses sensible defaults. Optionally create `.env.dev`:
```bash
cp .env.dev.example .env.dev
# Edit as needed
```

Then run with:
```bash
docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d
```

## Production Setup

### Prerequisites
1. **Create environment file**:
   ```bash
   cp .env.prod.example .env.prod
   ```

2. **Edit `.env.prod`** with secure values:
   ```bash
   # Generate strong passwords
   POSTGRES_PASSWORD=$(openssl rand -base64 32)
   ADMIN_PASSWORD=$(openssl rand -base64 24)
   JWT_SECRET=$(openssl rand -base64 32)
   ```

### Deployment
```bash
# Pull latest images
docker pull ghcr.io/clertonraf/kraftlog-api:latest
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:latest

# Start production environment (all services)
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f app
docker-compose -f docker-compose.prod.yml logs -f pdf-import

# Stop
docker-compose -f docker-compose.prod.yml down
```

### Features
- **Uses published images**: Pulls from GitHub Container Registry
- **Secure by default**: Requires strong passwords in .env
- **Production database**: PostgreSQL on port 5432
- **Health checks**: Automatic restart on failure
- **Persistent data**: Named volumes for database
- **PDF Import included**: Automatically configured to connect to API

### Accessing Services

Once started, you can access:
- **Main API**: http://localhost:8080
- **API Swagger UI**: http://localhost:8080/swagger-ui.html
- **PDF Import**: http://localhost:8081
- **PDF Import Swagger UI**: http://localhost:8081/swagger-ui.html

### Security Checklist
- ✅ Strong database password (30+ characters)
- ✅ Strong admin password (20+ characters)
- ✅ Unique JWT secret (32+ characters)
- ✅ Environment file permissions: `chmod 600 .env.prod`
- ✅ Use HTTPS in production (reverse proxy)
- ✅ Regular backups of PostgreSQL data

## Common Commands

### Development
```bash
# Start
docker-compose -f docker-compose.dev.yml up -d

# Rebuild after code changes
docker-compose -f docker-compose.dev.yml up -d --build

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop and remove volumes (fresh start)
docker-compose -f docker-compose.dev.yml down -v

# Execute commands in container
docker-compose -f docker-compose.dev.yml exec app bash
```

### Production
```bash
# Start
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# Update to latest image
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop (keeps data)
docker-compose -f docker-compose.prod.yml down

# Backup database
docker exec kraftlog-postgres-prod pg_dump -U kraftlog kraftlog > backup.sql

# Restore database
docker exec -i kraftlog-postgres-prod psql -U kraftlog kraftlog < backup.sql
```

## Differences: Dev vs Prod

| Feature | Development | Production |
|---------|------------|------------|
| **Image Source** | Built locally | Pulled from GHCR |
| **Database Port** | 5433 | 5432 |
| **Log Level** | DEBUG | INFO |
| **Default Passwords** | Yes (insecure) | No (must provide) |
| **Auto-restart** | No | Yes |
| **Health Checks** | Basic | Comprehensive |
| **Volumes** | Code mounted | Data only |
| **Container Names** | *-dev | *-prod |

## Network Architecture

Both configurations create an isolated network:
```
kraftlog-network (bridge)
├── postgres (internal: 5432)
│   └── External: 5433 (dev) / 5432 (prod)
└── app (internal: 8080)
    └── External: 8080
```

Applications communicate via service names (e.g., `postgres:5432`).

## Troubleshooting

### Port already in use
```bash
# Check what's using the port
lsof -i :8080

# Use different port
# Edit docker-compose file and change ports: "8081:8080"
```

### Database connection failed
```bash
# Check if postgres is healthy
docker-compose -f docker-compose.*.yml ps

# View postgres logs
docker-compose -f docker-compose.*.yml logs postgres

# Ensure app waits for postgres health check
```

### Image pull failed (production)
```bash
# Login to GitHub Container Registry
echo YOUR_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull manually
docker pull ghcr.io/clertonraf/kraftlog-api:latest
```

## Environment Variables Reference

### Required (Production)
- `POSTGRES_PASSWORD` - Database password
- `ADMIN_PASSWORD` - Admin user password
- `JWT_SECRET` - JWT signing secret

### Optional
- `POSTGRES_DB` - Database name (default: kraftlog)
- `POSTGRES_USER` - Database user (default: kraftlog)
- `ADMIN_USERNAME` - Admin username (default: admin)
- `ADMIN_EMAIL` - Admin email
- `JWT_EXPIRATION` - Token expiration in ms (default: 86400000)
- `LOG_LEVEL` - Root log level (default: INFO)
- `APP_LOG_LEVEL` - Application log level (default: INFO)

## Next Steps

### Development
1. `docker-compose -f docker-compose.dev.yml up -d`
2. Access API: http://localhost:8080
3. Access Swagger: http://localhost:8080/swagger-ui.html
4. Make code changes and rebuild

### Production
1. Copy and configure `.env.prod`
2. Pull latest image
3. `docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d`
4. Set up reverse proxy (Nginx/Caddy) with SSL
5. Configure backups
6. Set up monitoring

## Support

For more information, see:
- Main README: `/README.md`
- API Documentation: http://localhost:8080/swagger-ui.html
- GitHub: https://github.com/clertonraf/KraftLogApi
