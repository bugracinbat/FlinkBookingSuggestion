#!/bin/bash

# Flink Booking Suggestion Application Runner Script

echo "==========================================="
echo "Flink Booking Suggestion Application"
echo "==========================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven to build the application"
    exit 1
fi

# Clean and build the project
echo "Building the application..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
    exit 1
fi

# Check if JAR file exists
JAR_FILE="target/booking-suggestion-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi

echo "Starting Flink Booking Suggestion Application..."
echo "Press Ctrl+C to stop the application"
echo "==========================================="

# Run the application
java -jar "$JAR_FILE" 