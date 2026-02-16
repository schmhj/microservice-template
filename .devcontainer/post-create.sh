#!/bin/bash

# Exit on any error
set -e

echo "Running post-create setup..."

# Setup Gradle directories
echo "Setting up Gradle directories..."
mkdir -p $HOME/.gradle
mkdir -p gradle/wrapper

# Set proper permissions
echo "Setting file permissions..."
chmod -R 755 gradle
chmod +x gradlew

# Setup Gradle wrapper
echo "Setting up Gradle wrapper..."
./gradlew wrapper --gradle-version 9.2.1

# Verify installations
echo "Verifying installations..."
./gradlew --version
java -version

# Install additional tools
echo "Installing additional tools..."
sudo apt-get update
sudo apt-get install -y git-lfs

# Build application
echo "Starting to build application..."
docker build -t microservice .

# Start Docker Compose services
echo "Starting Docker Compose services (PostgreSQL and Grafana LGTM stack)..."
docker compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 10

echo "Checking service status..."
docker compose ps

echo "Post-create setup completed successfully!"
