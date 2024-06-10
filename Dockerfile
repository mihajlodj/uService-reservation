# Base image with JDK and Gradle
FROM gradle:7.5.0-jdk17 as build

# Set working directory
WORKDIR /home/reservation-service

# Copy build files
COPY build.gradle settings.gradle /home/reservation-service/
COPY src /home/reservation-service/src

# Build the project
RUN gradle build --no-daemon

# Using OpenJDK 17 for the runtime
FROM openjdk:17-slim

ARG SERVICE_NAME="reservation-service"

# Set the deployment directory
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /home/reservation-service/build/libs/$SERVICE_NAME.jar /app/app.jar

# Expose the port
EXPOSE 8082

# Define the entry point
ENTRYPOINT ["java", "-Xms256m", "-Xmx3072m", "-jar", "app.jar"]
