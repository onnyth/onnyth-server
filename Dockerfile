# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml only (layer caching optimization)
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
# Skip tests here for faster builds; they should run in CI
RUN mvn clean package -DskipTests -q \
    && JAR_PATH="$(ls target/*.jar | grep -v '\\.original$' | head -n 1)" \
    && cp "$JAR_PATH" target/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Runtime healthcheck uses wget
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /build/target/app.jar app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app

# Use spring user
USER spring

# Expose port
EXPOSE 8080

# Health check - uses Spring Boot Actuator /actuator/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]