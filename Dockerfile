# Dockerfile for Flink Booking Suggestion Application

# Use OpenJDK 11 as base image
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY target/booking-suggestion-1.0.0.jar /app/booking-suggestion.jar

# Copy configuration files
COPY src/main/resources/application.properties /app/application.properties

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms1g"
ENV FLINK_PARALLELISM=4

# Expose port for Flink Web UI (if needed)
EXPOSE 8081

# Create a non-root user
RUN useradd -m -s /bin/bash flinkuser
RUN chown -R flinkuser:flinkuser /app
USER flinkuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/overview || exit 1

# Run the application
CMD ["java", "-jar", "/app/booking-suggestion.jar"] 