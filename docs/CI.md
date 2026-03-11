# CI/CD Pipeline Documentation

This document describes the CI/CD workflows implemented for this microservice. The pipeline is designed to provide rapid feedback during development and automated, versioned releases for production.

---

## 🏗️ CI Pipeline (`ci.yml`)

The CI Pipeline focuses on validating code changes and providing development-ready Docker images.

### Triggers
- **Pull Requests**: To `main` and `develop` branches.
- **Pushes**: To the `develop` branch.

### Jobs

#### 1. Lint and Test
- **Runs on**: Every trigger (PR and Push).
- **Steps**:
    - Sets up Java 25 (Temurin).
    - Caches Gradle dependencies for faster subsequent runs.
    - Runs `./gradlew test` (unit tests).
    - Runs `./gradlew check` (linting/quality checks).
    - Runs `./gradlew build` (full Gradle build).

#### 2. Build and Push (Development)
- **Runs on**: Pushes to `develop` only (via `if: github.event_name == 'push'`).
- **Purpose**: Provides a fresh Docker image for the test environment.
- **Steps**:
    - Logs into GitHub Container Registry (GHCR).
    - Generates metadata tags:
        - `type=ref,event=branch` (e.g., `ghcr.io/...:develop`)
        - `type=sha,format=long` (e.g., `ghcr.io/...:<commit-sha>`)
    - Builds and pushes the Docker image to GHCR.

---

## 🚀 Release Workflow (`release.yml`)

The Release Workflow automates versioning and pushes production-ready images to GHCR.

### Triggers
- **Pushes**: To the `main` branch.

### Jobs

#### 1. Release
- **Steps**:
    - Runs **Semantic Release** using Conventional Commits.
    - Determines the next semantic version (patch, minor, or major).
    - Creates a new Git tag and a GitHub Release with auto-generated release notes.
    - Outputs the new version to the next job.

#### 2. Build and Push (Release)
- **Runs on**: Only if a new release was published.
- **Steps**:
    - Logs into GHCR.
    - Generates metadata tags:
        - `type=semver,pattern={{version}}` (e.g., `ghcr.io/...:v1.2.3`)
        - `type=raw,value=latest` (e.g., `ghcr.io/...:latest`)
    - Builds and pushes the production Docker image to GHCR.

---

## 💡 Best Practices
- **Commit Messages**: Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) (e.g., `feat:`, `fix:`, `feat!:`) to trigger the correct automated versioning.
- **Development Feedback**: Rely on the CI pipeline for quick validation of PRs.
- **Production Safety**: Only merges to `main` trigger a formal release and production image push.
