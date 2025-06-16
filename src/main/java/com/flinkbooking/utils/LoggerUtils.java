package com.flinkbooking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Enhanced logging utilities for structured logging and performance tracking
 */
public class LoggerUtils {
    
    // Specialized loggers for different domains
    public static final Logger EVENTS_LOGGER = LoggerFactory.getLogger("com.flinkbooking.events");
    public static final Logger SUGGESTIONS_LOGGER = LoggerFactory.getLogger("com.flinkbooking.suggestions");
    public static final Logger ANALYTICS_LOGGER = LoggerFactory.getLogger("com.flinkbooking.analytics");
    public static final Logger METRICS_LOGGER = LoggerFactory.getLogger("com.flinkbooking.metrics");
    
    // MDC Keys
    public static final String USER_ID = "userId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String EVENT_TYPE = "eventType";
    public static final String SUGGESTION_ID = "suggestionId";
    public static final String CONFIDENCE_SCORE = "confidenceScore";
    public static final String PROCESSING_TIME = "processingTime";
    public static final String COMPONENT = "component";
    
    /**
     * Set correlation ID for tracking requests
     */
    public static String setCorrelationId() {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(CORRELATION_ID, correlationId);
        return correlationId;
    }
    
    /**
     * Set user context for logging
     */
    public static void setUserContext(String userId) {
        MDC.put(USER_ID, userId);
    }
    
    /**
     * Set component context
     */
    public static void setComponent(String componentName) {
        MDC.put(COMPONENT, componentName);
    }
    
    /**
     * Clear all MDC context
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Log booking event with structured data
     */
    public static void logBookingEvent(String userId, String eventType, String destination, 
                                     Double price, String correlationId) {
        try {
            MDC.put(USER_ID, userId);
            MDC.put(EVENT_TYPE, eventType);
            MDC.put(CORRELATION_ID, correlationId != null ? correlationId : setCorrelationId());
            
            EVENTS_LOGGER.info("Booking event processed - User: {}, Type: {}, Destination: {}, Price: ${}", 
                             userId, eventType, destination, price);
        } finally {
            // Don't clear MDC here as it might be used by downstream processing
        }
    }
    
    /**
     * Log suggestion generation with details
     */
    public static void logSuggestionGenerated(String userId, String suggestionId, String destination,
                                            Double confidenceScore, int numHotels, String reason) {
        try {
            MDC.put(USER_ID, userId);
            MDC.put(SUGGESTION_ID, suggestionId);
            MDC.put(CONFIDENCE_SCORE, String.valueOf(confidenceScore));
            
            SUGGESTIONS_LOGGER.info("Suggestion generated - User: {}, ID: {}, Destination: {}, " +
                                  "Confidence: {}%, Hotels: {}, Reason: {}", 
                                  userId, suggestionId, destination, confidenceScore, numHotels, reason);
        } finally {
            // Keep MDC for potential chaining
        }
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformanceMetric(String operation, long processingTimeMs, String details) {
        try {
            MDC.put(PROCESSING_TIME, String.valueOf(processingTimeMs));
            MDC.put("operation", operation);
            
            METRICS_LOGGER.info("Performance metric - Operation: {}, Duration: {}ms, Details: {}", 
                              operation, processingTimeMs, details);
        } finally {
            MDC.remove(PROCESSING_TIME);
            MDC.remove("operation");
        }
    }
    
    /**
     * Log user behavior analytics update
     */
    public static void logUserBehaviorUpdate(String userId, long totalSearches, long totalBookings, 
                                           double conversionRate, String topDestination) {
        try {
            MDC.put(USER_ID, userId);
            MDC.put("totalSearches", String.valueOf(totalSearches));
            MDC.put("totalBookings", String.valueOf(totalBookings));
            MDC.put("conversionRate", String.format("%.2f", conversionRate));
            
            ANALYTICS_LOGGER.info("User behavior updated - User: {}, Searches: {}, Bookings: {}, " +
                                "Conversion: {}%, Top Destination: {}", 
                                userId, totalSearches, totalBookings, conversionRate, topDestination);
        } finally {
            // Keep user context
            MDC.remove("totalSearches");
            MDC.remove("totalBookings");
            MDC.remove("conversionRate");
        }
    }
    
    /**
     * Log windowed analytics
     */
    public static void logWindowedAnalytics(String windowType, long eventCount, 
                                          Map<String, Long> eventTypeCounts, String timeWindow) {
        try {
            MDC.put("windowType", windowType);
            MDC.put("eventCount", String.valueOf(eventCount));
            MDC.put("timeWindow", timeWindow);
            
            ANALYTICS_LOGGER.info("Windowed analytics - Type: {}, Events: {}, Window: {}, Breakdown: {}", 
                                windowType, eventCount, timeWindow, eventTypeCounts);
        } finally {
            MDC.remove("windowType");
            MDC.remove("eventCount");
            MDC.remove("timeWindow");
        }
    }
    
    /**
     * Log error with context
     */
    public static void logError(Logger logger, String message, Throwable throwable, 
                              Map<String, String> context) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            logger.error(message, throwable);
        } finally {
            if (context != null) {
                context.keySet().forEach(MDC::remove);
            }
        }
    }
    
    /**
     * Create a timer for performance measurement
     */
    public static Timer startTimer(String operation) {
        return new Timer(operation);
    }
    
    /**
     * Simple timer class for performance measurement
     */
    public static class Timer {
        private final String operation;
        private final long startTime;
        
        public Timer(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        public long stop() {
            long duration = System.currentTimeMillis() - startTime;
            logPerformanceMetric(operation, duration, "");
            return duration;
        }
        
        public long stopWithDetails(String details) {
            long duration = System.currentTimeMillis() - startTime;
            logPerformanceMetric(operation, duration, details);
            return duration;
        }
    }
    
    /**
     * Helper to create context map
     */
    public static Map<String, String> createContext() {
        return new HashMap<>();
    }
    
    /**
     * Helper to add context entry
     */
    public static Map<String, String> addContext(Map<String, String> context, String key, String value) {
        context.put(key, value);
        return context;
    }
} 