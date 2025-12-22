# GitHub Actions Workflows

This repository uses GitHub Actions to automate Docker image builds and deployments.

## Workflows

### Docker Build and Push (`docker-build.yml`)

Automatically builds and pushes Docker images to GitHub Container Registry (GHCR) with proper versioning.

#### Triggers

- **Push to `main` branch**: Creates `latest` tag
- **Push to `develop` branch**: Creates `develop` tag
- **Git tags (v*.*.*)**: Creates version tags (e.g., `v1.1.0` → `1.1.0`, `1.1`, `1`)
- **Pull Requests to `main`**: Builds without pushing (validation)

#### Versioning Strategy

The workflow automatically creates multiple tags:

1. **Branch-based tags**:
   - `latest` - Latest stable release (main branch)
   - `develop` - Latest development build

2. **Semantic version tags** (from git tags):
   - `1.1.0` - Exact version
   - `1.1` - Major.minor version
   - `1` - Major version

3. **Commit SHA tags**:
   - `main-abc1234` - Branch + short commit SHA

#### Examples

**Scenario 1: Push to main branch**
```bash
git push origin main
```
**Creates**: `ghcr.io/clertonraf/kraftlog-api:latest`

**Scenario 2: Create a version tag**
```bash
git tag v1.2.0
git push origin v1.2.0
```
**Creates**:
- `ghcr.io/clertonraf/kraftlog-api:1.2.0`
- `ghcr.io/clertonraf/kraftlog-api:1.2`
- `ghcr.io/clertonraf/kraftlog-api:1`
- `ghcr.io/clertonraf/kraftlog-api:latest`

**Scenario 3: Push to develop branch**
```bash
git checkout develop
git push origin develop
```
**Creates**: `ghcr.io/clertonraf/kraftlog-api:develop`

#### Image Details

- **Registry**: GitHub Container Registry (ghcr.io)
- **Repository**: `ghcr.io/clertonraf/kraftlog-api`
- **Platforms**: linux/amd64, linux/arm64
- **Cache**: GitHub Actions cache for faster builds

#### Using the Images

```bash
# Latest stable version
docker pull ghcr.io/clertonraf/kraftlog-api:latest

# Specific version
docker pull ghcr.io/clertonraf/kraftlog-api:1.1.0

# Development version
docker pull ghcr.io/clertonraf/kraftlog-api:develop

# Specific commit
docker pull ghcr.io/clertonraf/kraftlog-api:main-abc1234
```

## Creating a New Release

### Option 1: Using Git Tags (Recommended)

1. **Update version in `pom.xml`**:
   ```xml
   <version>1.2.0</version>
   ```

2. **Commit the version change**:
   ```bash
   git add pom.xml
   git commit -m "Bump version to 1.2.0"
   ```

3. **Create and push the tag**:
   ```bash
   git tag v1.2.0
   git push origin main
   git push origin v1.2.0
   ```

4. **GitHub Actions will**:
   - Build the Docker image
   - Tag it as `1.2.0`, `1.2`, `1`, and `latest`
   - Push to GHCR

### Option 2: GitHub Releases

1. Go to: https://github.com/clertonraf/KraftLogApi/releases/new
2. Click "Choose a tag" and create new tag: `v1.2.0`
3. Fill in release notes
4. Click "Publish release"
5. GitHub Actions will automatically build and push the image

## Monitoring Builds

- **View workflow runs**: https://github.com/clertonraf/KraftLogApi/actions
- **View published images**: https://github.com/clertonraf/KraftLogApi/pkgs/container/kraftlog-api

## Troubleshooting

### Build Fails

1. Check the Actions tab for error logs
2. Ensure Dockerfile is correct
3. Verify all dependencies are accessible

### Image Not Visible

1. Go to package settings: https://github.com/clertonraf/KraftLogApi/pkgs/container/kraftlog-api/settings
2. Change visibility to "Public" if needed
3. Check package permissions

### Cannot Pull Image

```bash
# Login to GHCR (if private)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Then pull
docker pull ghcr.io/clertonraf/kraftlog-api:latest
```

## Rollback

If a release has issues, roll back using a previous version:

```bash
# List available versions
docker pull ghcr.io/clertonraf/kraftlog-api:1.0.0

# Update docker-compose.prod.yml to use specific version
image: ghcr.io/clertonraf/kraftlog-api:1.0.0
```

## Security

- The workflow uses `GITHUB_TOKEN` which is automatically provided by GitHub
- No manual secrets needed for GHCR authentication
- Images are scanned for vulnerabilities
- Build provenance is attached to images

## Advanced: Manual Trigger

To manually trigger a build without pushing:

```bash
gh workflow run docker-build.yml
```

Or use the GitHub UI:
1. Go to Actions tab
2. Select "Build and Push Docker Image"
3. Click "Run workflow"

## Best Practices

1. **Always tag releases**: Use semantic versioning (v1.2.3)
2. **Update pom.xml version**: Keep it in sync with git tags
3. **Test before releasing**: Use pull requests to validate builds
4. **Use develop branch**: For testing before merging to main
5. **Document changes**: Use GitHub releases for release notes

## Integration with Docker Compose

Update `docker-compose.prod.yml` to use specific versions:

```yaml
services:
  app:
    # Use latest
    image: ghcr.io/clertonraf/kraftlog-api:latest
    
    # Or use specific version
    image: ghcr.io/clertonraf/kraftlog-api:1.1.0
```

## Version Strategy

```
main branch (stable)
  ↓ push
  → ghcr.io/clertonraf/kraftlog-api:latest
  
v1.2.0 tag
  ↓ push
  → ghcr.io/clertonraf/kraftlog-api:1.2.0
  → ghcr.io/clertonraf/kraftlog-api:1.2
  → ghcr.io/clertonraf/kraftlog-api:1
  
develop branch (testing)
  ↓ push
  → ghcr.io/clertonraf/kraftlog-api:develop
```

## Support

For issues with GitHub Actions:
- Check workflow file: `.github/workflows/docker-build.yml`
- View action logs in the Actions tab
- Consult GitHub Actions documentation: https://docs.github.com/actions
