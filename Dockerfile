# Stage 1: Build
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app
COPY . .
# Build the JAR, skipping tests for speed
RUN ./gradlew clean build -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the JAR from the build stage
# Note: Adjust the path if your main class is in the infrastructure module
COPY --from=build /app/build/libs/*.jar app.jar

# Optimize for container environment
ENTRYPOINT ["java", "-jar", "/app/app.jar"]