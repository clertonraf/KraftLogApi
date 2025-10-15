# Build stage
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine

# Add metadata labels
LABEL org.opencontainers.image.title="KraftLog API"
LABEL org.opencontainers.image.description="Gym Exercise Logging REST API with JWT authentication"
LABEL org.opencontainers.image.vendor="KraftLog"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.source="https://github.com/clertonraf/KraftLogApi"
LABEL org.opencontainers.image.documentation="https://github.com/clertonraf/KraftLogApi/blob/main/README.md"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S kraftlog && adduser -S kraftlog -G kraftlog

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown kraftlog:kraftlog app.jar

# Switch to non-root user
USER kraftlog

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]