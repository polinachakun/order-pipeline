# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR from the Maven build
COPY target/warehouse-service.jar warehouse-service.jar

# Expose the port that Spring Boot runs on
EXPOSE 8084

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "warehouse-service.jar"]

