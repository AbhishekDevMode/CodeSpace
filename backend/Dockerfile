# Stage 1: Build the Spring Boot Application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default port (Render will override via $PORT)
EXPOSE 8080

# Use shell form to pass $PORT and environment variables dynamically
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]