# Database Cleanup Scripts

This directory contains scripts to clean and reset the KraftLog database.

## Scripts Available

### 1. `clean-database.sh` - Complete Database Recreation
**What it does:**
- Drops ALL tables including Flyway history
- Recreates the entire database schema using Flyway migrations
- Most thorough cleanup option

**Usage:**
```bash
cd /Users/clerton/workspace/KraftLogApi
./clean-database.sh
```

**When to use:**
- When you need a completely fresh start
- After changing database migrations
- When the schema is corrupted

---

### 2. `reset-database.sh` - Quick Data Reset
**What it does:**
- Truncates (empties) all data tables
- Keeps the table structure intact
- Faster than full recreation

**Usage:**
```bash
cd /Users/clerton/workspace/KraftLogApi
./reset-database.sh
```

**When to use:**
- When you just need to delete all data
- For quick testing iterations
- Schema is fine, just want empty tables

---

### 3. `clean-database.sql` - Manual SQL Script
**What it does:**
- Same as `reset-database.sh` but as pure SQL
- Can be executed directly in psql or pgAdmin

**Usage with psql:**
```bash
PGPASSWORD=postgres psql -h localhost -p 5433 -U postgres -d kraftlog -f clean-database.sql
```

**Usage with Docker:**
```bash
cat clean-database.sql | docker exec -i kraftlog-postgres psql -U postgres -d kraftlog
```

**When to use:**
- When you prefer SQL over bash scripts
- When running from a database client
- For automation in CI/CD

---

## Prerequisites

All scripts require:
1. ✅ PostgreSQL container running: `docker-compose up -d postgres`
2. ✅ `psql` command-line tool installed (for bash scripts)

## After Cleanup

After running any cleanup script:

1. **Restart the Spring Boot application** to recreate the admin user:
   ```bash
   # Stop current app
   pkill -f "spring-boot:run"
   
   # Start fresh
   cd /Users/clerton/workspace/KraftLogApi
   bash -c 'source ~/.sdkman/bin/sdkman-init.sh && sdk use java 17.0.13-tem && mvn spring-boot:run'
   ```

2. **Default admin credentials** will be recreated:
   - Email: `admin@kraftlog.com`
   - Password: `admin123`

## Troubleshooting

### "PostgreSQL container is not running"
```bash
cd /Users/clerton/workspace/KraftLogApi
docker-compose up -d postgres
```

### "psql: command not found"
Install PostgreSQL client:
```bash
brew install postgresql
```

### Permission denied
Make scripts executable:
```bash
chmod +x clean-database.sh reset-database.sh
```

## Safety Features

- ⚠️ All scripts require confirmation before deleting data
- ✅ Scripts check if PostgreSQL is running
- ✅ Error handling to prevent partial deletions
- ✅ Status messages to track progress

## Quick Reference

| Script | Speed | Thoroughness | Use Case |
|--------|-------|-------------|----------|
| `reset-database.sh` | ⚡ Fast | 🧹 Data only | Quick testing |
| `clean-database.sh` | 🐌 Slow | 🔥 Complete | Schema changes |
| `clean-database.sql` | ⚡ Fast | 🧹 Data only | Manual/DB client |
