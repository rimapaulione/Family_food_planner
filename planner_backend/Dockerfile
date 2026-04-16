# syntax=docker/dockerfile:1

# ============================================================
# STAGE 1 — "builder": has Maven + full JDK, compiles the jar
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first and download dependencies.
# Docker caches this layer — if pom.xml doesn't change,
# the next build skips re-downloading everything.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the jar.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2 — "runtime": small image, only JRE, just runs the jar
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy ONLY the built jar from the builder stage.
COPY --from=builder /app/target/*.jar app.jar

# Documents that this container listens on port 8080 internally.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
