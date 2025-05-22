# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /workspace/app

# Copy maven files for dependency resolution
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make the mvnw script executable
RUN chmod +x ./mvnw

# Build the application with Maven
RUN ./mvnw install -DskipTests

# Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar file built in the previous stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Set environment variables for the application
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
