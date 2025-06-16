package com.flinkbooking;

import com.flinkbooking.aggregators.EventCountAggregator;
import com.flinkbooking.model.BookingEvent;
import com.flinkbooking.model.BookingSuggestion;
import com.flinkbooking.processors.BookingSuggestionProcessor;
import com.flinkbooking.sources.BookingEventSource;
import com.flinkbooking.utils.LoggerUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application class for the Flink Booking Suggestion System
 */
public class BookingSuggestionApp {
    
    private static final Logger LOG = LoggerFactory.getLogger(BookingSuggestionApp.class);
    
    public static void main(String[] args) throws Exception {
        
        // Initialize logging context
        LoggerUtils.setComponent("BookingSuggestionApp");
        String appCorrelationId = LoggerUtils.setCorrelationId();
        
        LOG.info("Starting Flink Booking Suggestion Application with correlation ID: {}", appCorrelationId);
        
        LoggerUtils.Timer appStartupTimer = LoggerUtils.startTimer("application_startup");
        
        try {
            // Set up the streaming execution environment
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            
            // Configure environment
            env.setParallelism(4); // Set parallelism for better performance
            env.enableCheckpointing(5000); // Enable checkpointing every 5 seconds
            
            LOG.info("Flink environment configured - Parallelism: 4, Checkpoint interval: 5000ms");
            LoggerUtils.logPerformanceMetric("environment_setup", 0, "Parallelism=4, Checkpointing=5000ms");
            
            // Create the data source - booking events
            LOG.info("Creating booking events source...");
            DataStream<BookingEvent> bookingEvents = env
                    .addSource(new BookingEventSource())
                    .name("Booking Events Source")
                    .uid("booking-events-source");
            
            LOG.info("Booking events source created successfully");
            
            // Monitor booking events and more frequent samples of other events
            bookingEvents
                    .filter(event -> "BOOK".equals(event.getEventType()) || 
                                   event.getUserId().hashCode() % 20 == 0) // Show ~5% of non-booking events
                    .map(event -> {
                        LoggerUtils.setUserContext(event.getUserId());
                        String eventIcon = "BOOK".equals(event.getEventType()) ? "🎯" : "📝";
                        return eventIcon + " " + event.getEventType() + " | User: " + event.getUserId() + 
                               " | Destination: " + event.getDestination() + " | Price: $" + event.getPrice();
                    })
                    .print("EVENTS")
                    .name("Print Events");
            
            // Process events by user to generate suggestions
            LOG.info("Setting up booking suggestion processor...");
            DataStream<BookingSuggestion> suggestions = bookingEvents
                    .keyBy(BookingEvent::getUserId) // Key by user ID
                    .process(new BookingSuggestionProcessor())
                    .name("Booking Suggestion Processor")
                    .uid("suggestion-processor");
            
            LOG.info("Booking suggestion processor configured successfully");
            
            // Enhanced suggestion monitoring
            suggestions
                    .map(suggestion -> {
                        return String.format("💡 SUGGESTION | User: %s | Destination: %s | Confidence: %.1f%% | Hotels: %d | Reason: %s",
                                           suggestion.getUserId(),
                                           suggestion.getDestination(),
                                           suggestion.getConfidenceScore(),
                                           suggestion.getRecommendedHotels().size(),
                                           suggestion.getSuggestionReason());
                    })
                    .print("SUGGESTIONS")
                    .name("Print Suggestions");
            
            // Simplified analytics - only show summary statistics
            LOG.info("Setting up analytics windows...");
            DataStream<String> analytics = bookingEvents
                    .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(30))) // Reduced to 30s for higher frequency
                    .aggregate(new EventCountAggregator())
                    .map(summary -> {
                        LoggerUtils.logWindowedAnalytics("30s_summary", summary.totalEvents, 
                                                       summary.eventTypeCounts, "30 seconds");
                        return String.format("📊 === 30s SUMMARY === Total: %d | Bookings: %d (%.1f%%) | Searches: %d | Views: %d | Cancels: %d",
                                           summary.totalEvents, 
                                           summary.eventTypeCounts.getOrDefault("BOOK", 0L),
                                           summary.totalEvents > 0 ? (summary.eventTypeCounts.getOrDefault("BOOK", 0L) * 100.0 / summary.totalEvents) : 0,
                                           summary.eventTypeCounts.getOrDefault("SEARCH", 0L),
                                           summary.eventTypeCounts.getOrDefault("VIEW", 0L),
                                           summary.eventTypeCounts.getOrDefault("CANCEL", 0L));
                    })
                    .name("Analytics Window");
            
            analytics.print("ANALYTICS");
            
            // Simplified event type monitoring - only show booking confirmations
            bookingEvents
                    .filter(event -> "BOOK".equals(event.getEventType()))
                    .map(event -> "🎯 BOOKING CONFIRMED: " + event.getUserId() + " → " + event.getDestination())
                    .print("BOOKINGS");
            
            // Log successful job configuration
            appStartupTimer.stopWithDetails("Flink job configuration completed");
            LOG.info("🚀 Flink job configured successfully. Starting execution...");
            
            // Create logs directory if it doesn't exist
            java.nio.file.Path logsDir = java.nio.file.Paths.get("logs");
            if (!java.nio.file.Files.exists(logsDir)) {
                java.nio.file.Files.createDirectories(logsDir);
                LOG.info("Created logs directory: {}", logsDir.toAbsolutePath());
            }
            
            // Execute the job
            LoggerUtils.Timer executionTimer = LoggerUtils.startTimer("job_execution");
            env.execute("Booking Suggestion Application");
            
        } catch (Exception e) {
            LoggerUtils.logError(LOG, "❌ Critical error executing Flink job", e,
                               LoggerUtils.addContext(LoggerUtils.createContext(), 
                                                    "correlationId", appCorrelationId));
            throw e;
        } finally {
            LOG.info("🏁 Flink Booking Suggestion Application shutdown completed");
            LoggerUtils.clearContext();
        }
    }
} 