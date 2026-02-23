# Multi-stage build for Spring Boot application with Java 21

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

# Copy the JAR from builder stage
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Set timezone (optional, adjust as needed)
ENV TZ=Asia/Ho_Chi_Minh

# Environment variables with defaults from application.properties
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/fnb_recommender_db \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres \
    JWT_SECRET=your-256-bit-secret-key-change-in-production-please-use-at-least-32-characters \
    JWT_ACCESS_EXPIRATION=900000 \
    JWT_REFRESH_EXPIRATION=604800000 \
    MAIL_USERNAME= \
    MAIL_PASSWORD= \
    GOOGLE_CLIENT_ID=dummy-client-id-replace-in-production \
    GOOGLE_CLIENT_SECRET=dummy-client-secret-replace-in-production \
    FACEBOOK_APP_ID=dummy-app-id-replace-in-production \
    FACEBOOK_APP_SECRET=dummy-app-secret-replace-in-production \
    FRONTEND_URL=http://localhost:3000 \
    SERVER_PORT=8080

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api-docs || exit 1

# Run the application with optimized JVM settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
