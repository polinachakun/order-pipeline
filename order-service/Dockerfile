# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR from the Maven build
COPY target/order-service-1.0.0-SNAPSHOT.jar order-service.jar


# Expose the port that Spring Boot runs on
EXPOSE 8092

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "order-service.jar"]
