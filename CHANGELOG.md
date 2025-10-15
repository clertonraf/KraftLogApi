# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **GitHub Container Registry Integration**
  - Automated Docker image building and publishing via GitHub Actions
  - Multi-architecture support (linux/amd64, linux/arm64)
  - Semantic versioning for Docker images
  - Image tagging strategy: version tags, branch tags, SHA tags, and latest tag
  - Build provenance attestation for supply chain security

- **Enhanced Dockerfile**
  - Added OCI metadata labels for better image documentation
  - Implemented non-root user (kraftlog) for improved security
  - Added health check endpoint monitoring using curl
  - Optimized multi-stage build process
  - Support for multi-platform builds (linux/amd64, linux/arm64)

### Changed
- Updated project version from 0.0.1-SNAPSHOT to 1.0.0
- Enhanced Docker image security with non-root user execution
- Switched base image from `eclipse-temurin:17-jre-alpine` to `eclipse-temurin:17-jre-jammy` for ARM64 support

### Fixed
- Fixed Docker tag format error in GitHub Actions workflow (changed from `{{branch}}-` prefix to `sha-` prefix)
- Fixed ARM64 platform support by switching from Alpine to Ubuntu Jammy base image
- Fixed Lombok boolean getter method name in CustomUserDetailsService (changed from `getIsAdmin()` to `isAdmin()`)

### CI/CD
- Created GitHub Actions workflow for automated Docker image builds
- Configured GitHub Container Registry (ghcr.io) as image repository
- Enabled automatic builds on push to main branch and version tags
- Added pull request build validation (build-only, no push)
- Implemented Docker layer caching for faster builds
- Added automated test execution before Docker image creation

## [1.0.0] - 2025-10-15

### Added
- **Administrator Role System**
  - Added `isAdmin` boolean field to User entity to distinguish admin users from regular users
  - Implemented automatic admin user creation on application startup via `AdminInitializer` component
  - Added configurable admin credentials through environment variables:
    - `ADMIN_USERNAME` (default: admin)
    - `ADMIN_PASSWORD` (default: admin123)
    - `ADMIN_EMAIL` (default: admin@kraftlog.com)
  - Created `.env.example` file to document admin configuration options

- **Admin-Only Exercise Management**
  - Restricted exercise creation endpoint (`POST /api/exercises`) to admin users only
  - Restricted exercise update endpoint (`PUT /api/exercises/{id}`) to admin users only
  - Restricted exercise deletion endpoint (`DELETE /api/exercises/{id}`) to admin users only
  - Regular users can still view all exercises

- **Admin Management Endpoints**
  - Added `DELETE /api/admin/users/{userId}` endpoint for admins to delete any user
  - Added `PUT /api/admin/users/{userId}/password` endpoint for admins to change any user's password
  - Created `AdminController` with `@PreAuthorize("hasRole('ADMIN')")` protection
  - Created `AdminService` to handle admin-specific business logic
  - Added `ChangePasswordRequest` DTO for password change operations

- **Database Changes**
  - Created Flyway migration `V3__add_admin_role.sql` to add `is_admin` column to users table
  - Added database index on `is_admin` column for efficient admin queries

- **Security Enhancements**
  - Updated `CustomUserDetailsService` to assign `ROLE_ADMIN` to admin users and `ROLE_USER` to regular users
  - Integrated Spring Security's `@PreAuthorize` annotation for role-based access control

- **Testing Support**
  - Added `adminUser()` builder method to `TestDataBuilder` for creating admin users in tests
  - Updated existing user builder to explicitly set `isAdmin = false` for regular users

- **Docker Compose Configuration**
  - Added admin environment variables to docker-compose.yml with sensible defaults
  - Environment variables are passed from host to container for flexible configuration

### Changed
- Updated User entity to include admin role flag with default value of `false`
- Enhanced JWT authentication to include role information (ROLE_ADMIN or ROLE_USER)
- Updated API documentation in README.md with comprehensive admin role documentation
- Improved Swagger/OpenAPI documentation with admin-only endpoint markers

### Documentation
- Added "Administrator Role" section to README.md covering:
  - Overview of admin privileges and capabilities
  - Admin initialization process
  - Three configuration options for setting admin credentials
  - Default admin credentials with security warnings
  - Complete admin operation examples with curl commands
  - Comparison table of admin vs regular user permissions
- Updated API Endpoints section to mark admin-only endpoints
- Added admin configuration to Environment Variables section
- Updated Database Migrations section to include V3 migration
- Enhanced features list to highlight administrator role functionality

### Security
- Admin password defaults are documented with strong recommendation to change on first login
- All admin operations require JWT authentication with ROLE_ADMIN authority
- Admin endpoints return 403 Forbidden when accessed by non-admin users