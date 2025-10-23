# ---- Build stage ----
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

# Copy Gradle wrapper & config first for caching
COPY build.gradle.kts settings.gradle.kts gradlew config.properties ./
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the rest of the source
COPY extensions ./extensions

# Build the fat jar (skip tests for faster builds, remove -x test if you want them)
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
COPY --from=builder /workspace/config.properties /app/config.properties

USER 65534

# Run the jar with config + logging
CMD ["java", "-Dedc.fs.config=/app/config.properties", "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", "-jar", "/app/federated-catalog.jar"]
