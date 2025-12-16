# ---- Build stage ----
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

# Copy Gradle wrapper & config first for caching
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the rest of the source
COPY extensions ./extensions

# Build the fat jar (tests run locally, skip in Docker for now due to file permission test)
RUN ./gradlew shadowJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install helper tools if needed
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl jq \
    && rm -rf /var/lib/apt/lists/*

# Copy only the fat jar and configs from builder
COPY --from=builder /workspace/build/libs/*.jar /app/federated-catalog.jar

# Default log level environment variable (can be overridden at runtime)
ENV LOG_LEVEL=info

USER 65534

# Run the jar with logging
CMD ["sh", "-c", "exec java -jar /app/federated-catalog.jar --log-level=$LOG_LEVEL"]
