package com.flinkbooking.sources;

import com.flinkbooking.model.BookingEvent;
import com.flinkbooking.utils.LoggerUtils;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom source function to generate sample booking events for testing
 */
public class BookingEventSource implements SourceFunction<BookingEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(BookingEventSource.class);
    private volatile boolean isRunning = true;
    private final Random random = new Random();
    private long eventCount = 0;
    private long bookingCount = 0;
    
    // Sample data
    private final List<String> users = Arrays.asList(
        "user_001", "user_002", "user_003", "user_004", "user_005",
        "user_006", "user_007", "user_008", "user_009", "user_010"
    );
    
    private final List<String> hotels = Arrays.asList(
        "hotel_001", "hotel_002", "hotel_003", "hotel_004", "hotel_005",
        "hotel_006", "hotel_007", "hotel_008", "hotel_009", "hotel_010"
    );
    
    private final List<String> destinations = Arrays.asList(
        "New York", "London", "Paris", "Tokyo", "Sydney",
        "Dubai", "Singapore", "Barcelona", "Rome", "Amsterdam"
    );
    
    private final List<String> eventTypes = Arrays.asList(
        "SEARCH", "VIEW", "BOOK", "CANCEL"
    );
    
    // Weighted event types to increase booking frequency
    private final List<String> weightedEventTypes = Arrays.asList(
        "SEARCH", "SEARCH", "SEARCH",  // 30% chance
        "VIEW", "VIEW", "VIEW",        // 30% chance  
        "BOOK", "BOOK", "BOOK", "BOOK", // 40% chance - increased from ~25%
        "CANCEL"                       // 10% chance - reduced
    );
    
    // High booking probability event types for peak booking periods
    private final List<String> highBookingEventTypes = Arrays.asList(
        "SEARCH", "SEARCH",            // 20% chance
        "VIEW", "VIEW",                // 20% chance  
        "BOOK", "BOOK", "BOOK", "BOOK", "BOOK", "BOOK", // 60% chance - very high booking rate
        "CANCEL"                       // 10% chance
    );
    
    private final List<String> roomTypes = Arrays.asList(
        "Single", "Double", "Suite", "Deluxe", "Presidential"
    );
    
    @Override
    public void run(SourceContext<BookingEvent> ctx) throws Exception {
        LoggerUtils.setComponent("BookingEventSource");
        LOG.info("Starting booking event source - generating synthetic booking events");
        
        long startTime = System.currentTimeMillis();
        long lastMetricsLogTime = startTime;
        
        while (isRunning) {
            try {
                String correlationId = LoggerUtils.setCorrelationId();
                
                // Generate random booking event with weighted event types
                String userId = users.get(random.nextInt(users.size()));
                String hotelId = hotels.get(random.nextInt(hotels.size()));
                
                // Use high booking probability during peak periods (every 10th event cycle)
                List<String> eventTypeList = (eventCount % 10 < 3) ? highBookingEventTypes : weightedEventTypes;
                String eventType = eventTypeList.get(random.nextInt(eventTypeList.size()));
                
                String destination = destinations.get(random.nextInt(destinations.size()));
                String roomType = roomTypes.get(random.nextInt(roomTypes.size()));
                
                LoggerUtils.setUserContext(userId);
                
                // Generate realistic dates
                LocalDate checkIn = LocalDate.now().plusDays(random.nextInt(30) + 1);
                LocalDate checkOut = checkIn.plusDays(random.nextInt(14) + 1);
                
                String checkInDate = checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String checkOutDate = checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE);
                
                // Generate realistic price based on destination and room type
                Double price = generatePrice(destination, roomType);
                
                // Generate realistic rating
                Double rating = 3.0 + random.nextDouble() * 2.0; // 3.0 to 5.0
                rating = Math.round(rating * 10.0) / 10.0; // Round to 1 decimal place
                
                BookingEvent event = new BookingEvent(
                    userId, hotelId, eventType, destination,
                    checkInDate, checkOutDate, price, rating, roomType
                );
                
                // Log booking events and more frequent samples for higher activity visibility
                if ("BOOK".equals(eventType) || eventCount % 50 == 0) {
                    LoggerUtils.logBookingEvent(userId, eventType, destination, price, correlationId);
                }
                
                // Track booking events specifically
                if ("BOOK".equals(eventType)) {
                    bookingCount++;
                }
                
                ctx.collect(event);
                eventCount++;
                
                // Log metrics every 2000 events (adjusted for higher frequency)
                if (eventCount % 2000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastLog = currentTime - lastMetricsLogTime;
                    double eventsPerSecond = 2000.0 / (timeSinceLastLog / 1000.0);
                    double bookingRate = eventCount > 0 ? (double) bookingCount / eventCount * 100 : 0;
                    
                    LOG.info("📊 Event Generation Metrics - {} events ({} bookings), Rate: {:.1f} events/sec, Booking Rate: {:.1f}%", 
                            eventCount, bookingCount, eventsPerSecond, bookingRate);
                    lastMetricsLogTime = currentTime;
                }
                
                // Emit events at different rates based on event type
                Thread.sleep(getDelayForEventType(eventType));
                
            } catch (Exception e) {
                LoggerUtils.logError(LOG, "Error generating booking event", e, 
                                   LoggerUtils.addContext(LoggerUtils.createContext(), 
                                                        "eventCount", String.valueOf(eventCount)));
                
                // Don't break the loop for individual event generation errors
                Thread.sleep(1000); // Wait a bit before retrying
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double finalBookingRate = eventCount > 0 ? (double) bookingCount / eventCount * 100 : 0;
        LOG.info("Booking event source stopped - Generated {} events ({} bookings, {:.1f}% booking rate) in {}ms", 
                eventCount, bookingCount, finalBookingRate, totalTime);
    }
    
    private Double generatePrice(String destination, String roomType) {
        double basePrice = 100.0;
        
        // Adjust price based on destination
        switch (destination) {
            case "New York":
            case "London":
            case "Paris":
            case "Tokyo":
                basePrice = 200.0;
                break;
            case "Dubai":
            case "Singapore":
                basePrice = 150.0;
                break;
            default:
                basePrice = 100.0;
        }
        
        // Adjust price based on room type
        switch (roomType) {
            case "Single":
                basePrice *= 1.0;
                break;
            case "Double":
                basePrice *= 1.3;
                break;
            case "Suite":
                basePrice *= 2.0;
                break;
            case "Deluxe":
                basePrice *= 2.5;
                break;
            case "Presidential":
                basePrice *= 4.0;
                break;
        }
        
        // Add some randomness
        basePrice += (random.nextDouble() - 0.5) * 50;
        
        return Math.max(50.0, Math.round(basePrice * 100.0) / 100.0);
    }
    
    private long getDelayForEventType(String eventType) {
        switch (eventType) {
            case "SEARCH":
                return 200; // Search events very frequent (reduced from 800ms)
            case "VIEW":
                return 300; // View events frequent (reduced from 1200ms)
            case "BOOK":
                return 400; // Booking events frequent (reduced from 1500ms)
            case "CANCEL":
                return 800; // Cancel events moderate (reduced from 4000ms)
            default:
                return 300;
        }
    }
    
    @Override
    public void cancel() {
        isRunning = false;
    }
} 