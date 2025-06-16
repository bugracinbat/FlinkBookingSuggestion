package com.flinkbooking.processors;

import com.flinkbooking.model.BookingEvent;
import com.flinkbooking.model.BookingSuggestion;
import com.flinkbooking.model.UserBehaviorAnalytics;
import com.flinkbooking.utils.LoggerUtils;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Processes booking events to generate personalized suggestions
 */
public class BookingSuggestionProcessor extends KeyedProcessFunction<String, BookingEvent, BookingSuggestion> {
    
    private static final Logger LOG = LoggerFactory.getLogger(BookingSuggestionProcessor.class);
    private transient ValueState<UserBehaviorAnalytics> userBehaviorState;
    private static final int MIN_EVENTS_FOR_SUGGESTION = 3;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        // Configure state TTL to prevent memory leaks
        StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(Time.days(30)) // Clean up user state after 30 days of inactivity
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();
        
        ValueStateDescriptor<UserBehaviorAnalytics> descriptor = new ValueStateDescriptor<>(
            "userBehavior",
            TypeInformation.of(new TypeHint<UserBehaviorAnalytics>() {})
        );
        
        // Apply TTL configuration
        descriptor.enableTimeToLive(ttlConfig);
        
        userBehaviorState = getRuntimeContext().getState(descriptor);
    }
    
    @Override
    public void processElement(BookingEvent event, Context ctx, Collector<BookingSuggestion> out) throws Exception {
        LoggerUtils.Timer timer = LoggerUtils.startTimer("process_booking_event");
        
        try {
            LoggerUtils.setComponent("BookingSuggestionProcessor");
            LoggerUtils.setUserContext(event.getUserId());
            
            // Only log debug info for booking events to reduce noise
            if ("BOOK".equals(event.getEventType())) {
                LOG.debug("Processing booking event - User: {}, Type: {}, Destination: {}", 
                         event.getUserId(), event.getEventType(), event.getDestination());
            }
            
            // Get current user behavior analytics or create new one
            UserBehaviorAnalytics userBehavior = userBehaviorState.value();
            boolean isNewUser = (userBehavior == null);
            
            if (userBehavior == null) {
                userBehavior = initializeUserBehavior(event.getUserId());
                LOG.info("Initialized new user behavior analytics for user: {}", event.getUserId());
            }
            
            // Update user behavior based on the event
            updateUserBehavior(userBehavior, event);
            
            // Log user behavior update only for significant events (bookings or every 10th search)
            if ("BOOK".equals(event.getEventType()) || userBehavior.getTotalSearches() % 10 == 0) {
                String topDestination = findTopDestination(userBehavior.getDestinationSearchCount());
                LoggerUtils.logUserBehaviorUpdate(
                    event.getUserId(),
                    userBehavior.getTotalSearches(),
                    userBehavior.getTotalBookings(),
                    userBehavior.getConversionRate(),
                    topDestination != null ? topDestination : "N/A"
                );
            }
            
            // Save updated state
            userBehaviorState.update(userBehavior);
            
            // Generate suggestion if we have enough data and this is a relevant event
            if (shouldGenerateSuggestion(userBehavior, event)) {
                LOG.info("Generating suggestion for user: {} after {} events", 
                        event.getUserId(), userBehavior.getTotalSearches());
                
                BookingSuggestion suggestion = generateSuggestion(userBehavior, event);
                if (suggestion != null) {
                    // Log suggestion generation
                    LoggerUtils.logSuggestionGenerated(
                        suggestion.getUserId(),
                        suggestion.getSuggestionId(),
                        suggestion.getDestination(),
                        suggestion.getConfidenceScore(),
                        suggestion.getRecommendedHotels().size(),
                        suggestion.getSuggestionReason()
                    );
                    
                    out.collect(suggestion);
                } else {
                    LOG.warn("Suggestion generation returned null for user: {}", event.getUserId());
                }
            } else {
                // Only log occasionally to avoid spam
                if (userBehavior.getTotalSearches() % 20 == 0) {
                    LOG.debug("Suggestion criteria not met for user: {} - Events: {}, Type: {}", 
                             event.getUserId(), userBehavior.getTotalSearches(), event.getEventType());
                }
            }
            
        } catch (Exception e) {
            LoggerUtils.logError(LOG, "Error processing booking event", e,
                               LoggerUtils.addContext(
                                   LoggerUtils.addContext(LoggerUtils.createContext(), 
                                                        "userId", event.getUserId()),
                                   "eventType", event.getEventType()));
            throw e;
        } finally {
            timer.stopWithDetails(String.format("User: %s, EventType: %s", 
                                               event.getUserId(), event.getEventType()));
        }
    }
    
    private UserBehaviorAnalytics initializeUserBehavior(String userId) {
        return new UserBehaviorAnalytics(
            userId,
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            0.0,
            0.0,
            "",
            0L,
            0L,
            ""
        );
    }
    
    private void updateUserBehavior(UserBehaviorAnalytics userBehavior, BookingEvent event) {
        String destination = event.getDestination();
        String hotelId = event.getHotelId();
        String eventType = event.getEventType();
        
        // Update preferred destinations
        userBehavior.getPreferredDestinations().add(destination);
        
        // Update destination search count
        Map<String, Long> destCount = userBehavior.getDestinationSearchCount();
        if (destCount == null) destCount = new HashMap<>();
        destCount.put(destination, destCount.getOrDefault(destination, 0L) + 1);
        userBehavior.setDestinationSearchCount(destCount);
        
        // Update hotel view count
        Map<String, Long> hotelCount = userBehavior.getHotelViewCount();
        if (hotelCount == null) hotelCount = new HashMap<>();
        hotelCount.put(hotelId, hotelCount.getOrDefault(hotelId, 0L) + 1);
        userBehavior.setHotelViewCount(hotelCount);
        
        // Update price preference
        if (event.getPrice() != null) {
            double currentAvg = userBehavior.getAveragePriceBudget() != null ? userBehavior.getAveragePriceBudget() : 0.0;
            long totalEvents = userBehavior.getTotalSearches() + 1;
            double newAvg = (currentAvg * (totalEvents - 1) + event.getPrice()) / totalEvents;
            userBehavior.setAveragePriceBudget(newAvg);
        }
        
        // Update rating preference
        if (event.getRating() != null) {
            double currentRating = userBehavior.getPreferredRating() != null ? userBehavior.getPreferredRating() : 0.0;
            long totalEvents = userBehavior.getTotalSearches() + 1;
            double newRating = (currentRating * (totalEvents - 1) + event.getRating()) / totalEvents;
            userBehavior.setPreferredRating(newRating);
        }
        
        // Update room type preference
        if (event.getRoomType() != null && !event.getRoomType().isEmpty()) {
            userBehavior.setPreferredRoomType(event.getRoomType());
        }
        
        // Update counters
        userBehavior.setTotalSearches(userBehavior.getTotalSearches() + 1);
        if ("BOOK".equals(eventType)) {
            userBehavior.setTotalBookings(userBehavior.getTotalBookings() + 1);
        }
        
        // Update last activity
        userBehavior.setLastActivity(event.getTimestamp());
        
        // Recalculate conversion rate
        long totalSearches = userBehavior.getTotalSearches();
        long totalBookings = userBehavior.getTotalBookings();
        double conversionRate = totalSearches > 0 ? (double) totalBookings / totalSearches * 100 : 0.0;
        userBehavior.setConversionRate(conversionRate);
    }
    
    private boolean shouldGenerateSuggestion(UserBehaviorAnalytics userBehavior, BookingEvent event) {
        // Generate suggestion after significant user activity
        return userBehavior.getTotalSearches() >= MIN_EVENTS_FOR_SUGGESTION
                && ("SEARCH".equals(event.getEventType()) || "VIEW".equals(event.getEventType()))
                && userBehavior.getTotalSearches() % 5 == 0; // Every 5th event
    }
    
    private BookingSuggestion generateSuggestion(UserBehaviorAnalytics userBehavior, BookingEvent event) {
        try {
            String userId = userBehavior.getUserId();
            
            // Find most searched destination
            String topDestination = findTopDestination(userBehavior.getDestinationSearchCount());
            if (topDestination == null) {
                topDestination = event.getDestination();
            }
            
            // Find top viewed hotels
            List<String> recommendedHotels = findTopHotels(userBehavior.getHotelViewCount(), 3);
            if (recommendedHotels.isEmpty()) {
                recommendedHotels = new ArrayList<>();
                recommendedHotels.add(event.getHotelId());
            }
            
            // Calculate suggestion metrics
            Double avgPrice = userBehavior.getAveragePriceBudget();
            Double avgRating = userBehavior.getPreferredRating();
            String preferredRoomType = userBehavior.getPreferredRoomType();
            
            // Generate suggestion reason
            String reason = generateSuggestionReason(userBehavior, topDestination);
            
            // Calculate confidence score
            Double confidenceScore = calculateConfidenceScore(userBehavior);
            
            return new BookingSuggestion(
                userId,
                topDestination,
                recommendedHotels,
                avgPrice,
                avgRating,
                preferredRoomType,
                reason,
                confidenceScore
            );
            
        } catch (Exception e) {
            // Log error and return null
            System.err.println("Error generating suggestion: " + e.getMessage());
            return null;
        }
    }
    
    private String findTopDestination(Map<String, Long> destinationCount) {
        if (destinationCount == null || destinationCount.isEmpty()) {
            return null;
        }
        
        return destinationCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    private List<String> findTopHotels(Map<String, Long> hotelCount, int limit) {
        if (hotelCount == null || hotelCount.isEmpty()) {
            return new ArrayList<>();
        }
        
        return hotelCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    private String generateSuggestionReason(UserBehaviorAnalytics userBehavior, String destination) {
        long totalSearches = userBehavior.getTotalSearches();
        double conversionRate = userBehavior.getConversionRate();
        
        if (conversionRate > 20) {
            return "Based on your high booking rate (" + String.format("%.1f", conversionRate) + 
                   "%), we recommend " + destination + " for your next trip.";
        } else if (totalSearches > 10) {
            return "You've shown strong interest in " + destination + 
                   " with " + totalSearches + " searches. Here are our top recommendations.";
        } else {
            return "Based on your recent activity, we suggest exploring " + destination + 
                   " with these highly-rated options.";
        }
    }
    
    private Double calculateConfidenceScore(UserBehaviorAnalytics userBehavior) {
        double score = 0.0;
        
        // Base score from activity level
        long totalSearches = userBehavior.getTotalSearches();
        if (totalSearches >= 10) {
            score += 40.0;
        } else if (totalSearches >= 5) {
            score += 25.0;
        } else {
            score += 10.0;
        }
        
        // Bonus for conversion rate
        double conversionRate = userBehavior.getConversionRate();
        if (conversionRate > 20) {
            score += 30.0;
        } else if (conversionRate > 10) {
            score += 20.0;
        } else if (conversionRate > 5) {
            score += 10.0;
        }
        
        // Bonus for destination consistency
        if (userBehavior.getPreferredDestinations().size() <= 3) {
            score += 20.0; // Focused preferences
        } else {
            score += 10.0; // Diverse preferences
        }
        
        // Bonus for recent activity
        score += 10.0; // Recent activity bonus
        
        return Math.min(100.0, score);
    }
} 