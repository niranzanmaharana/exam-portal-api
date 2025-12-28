# Multi-stage build for Exam Portal API
# Stage 1: Build the application as JAR (with embedded Tomcat for Docker)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Modify pom.xml for Docker build:
# 1. Change packaging from war to jar
# 2. Remove Tomcat exclusion
# 3. Change Tomcat scope from provided to compile
RUN sed -i 's/<packaging>war<\/packaging>/<packaging>jar<\/packaging>/' pom.xml && \
    sed -i '/<!-- Exclude embedded Tomcat/,/<\/exclusion>/d' pom.xml && \
    sed -i '/spring-boot-starter-tomcat/,/provided/s/<scope>provided<\/scope>/<scope>compile<\/scope>/' pom.xml

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build as executable JAR with embedded Tomcat
RUN mvn clean package spring-boot:repackage -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from build stage
COPY --from=build /app/target/exam-portal-api-*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:9090/exam-portal-api/api/auth/login || exit 1

# Run the application with Docker profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

