#!/bin/bash

# Flink Booking Suggestion Application Runner
# This script runs the application with proper JVM configuration for Java 11+

echo "🚀 Starting Flink Booking Suggestion Application..."

# Java 11+ Module System Compatibility Arguments
JVM_ARGS="--add-opens java.base/java.util=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.util.concurrent=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.lang=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.lang.invoke=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.math=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.net=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.nio=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.text=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.time=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.util.regex=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED"

# Memory Configuration
MEMORY_ARGS="-Xms512m -Xmx2g"

# Flink Configuration
FLINK_ARGS="-Djava.awt.headless=true"

# Create logs directory if it doesn't exist
mkdir -p logs

echo "📦 Building application..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
    echo "🎯 Running with Java module compatibility fixes..."
    
    # Run the application with all necessary JVM arguments
    java $JVM_ARGS $MEMORY_ARGS $FLINK_ARGS \
         -cp target/booking-suggestion-1.0.0.jar \
         com.flinkbooking.BookingSuggestionApp
         
else
    echo "❌ Build failed. Please check for compilation errors."
    exit 1
fi 