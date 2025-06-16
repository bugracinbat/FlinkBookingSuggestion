package com.flinkbooking;

import com.flinkbooking.model.BookingEvent;
import com.flinkbooking.model.BookingSuggestion;
import com.flinkbooking.model.UserBehaviorAnalytics;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Basic tests for the Booking Suggestion Application
 */
public class BookingSuggestionAppTest {
    
    @Test
    public void testBookingEventCreation() {
        BookingEvent event = new BookingEvent(
            "user_001", 
            "hotel_001", 
            "SEARCH",
            "Paris",
            "2024-06-01",
            "2024-06-05",
            200.0,
            4.5,
            "Double"
        );
        
        assertNotNull(event);
        assertEquals("user_001", event.getUserId());
        assertEquals("hotel_001", event.getHotelId());
        assertEquals("SEARCH", event.getEventType());
        assertEquals("Paris", event.getDestination());
        assertEquals(200.0, event.getPrice(), 0.01);
        assertEquals(4.5, event.getRating(), 0.01);
        assertEquals("Double", event.getRoomType());
        assertNotNull(event.getBookingId());
        assertNotNull(event.getTimestamp());
    }
    
    @Test
    public void testBookingSuggestionCreation() {
        List<String> hotels = Arrays.asList("hotel_001", "hotel_002", "hotel_003");
        
        BookingSuggestion suggestion = new BookingSuggestion(
            "user_001",
            "Paris",
            hotels,
            250.0,
            4.2,
            "Suite",
            "Based on your preferences",
            85.0
        );
        
        assertNotNull(suggestion);
        assertEquals("user_001", suggestion.getUserId());
        assertEquals("Paris", suggestion.getDestination());
        assertEquals(3, suggestion.getRecommendedHotels().size());
        assertEquals(250.0, suggestion.getAveragePrice(), 0.01);
        assertEquals(4.2, suggestion.getAverageRating(), 0.01);
        assertEquals("Suite", suggestion.getPreferredRoomType());
        assertEquals(85.0, suggestion.getConfidenceScore(), 0.01);
        assertNotNull(suggestion.getSuggestionId());
        assertNotNull(suggestion.getTimestamp());
    }
    
    @Test
    public void testUserBehaviorAnalytics() {
        Set<String> destinations = new HashSet<>(Arrays.asList("Paris", "London"));
        Map<String, Long> searchCount = new HashMap<>();
        searchCount.put("Paris", 5L);
        searchCount.put("London", 3L);
        
        Map<String, Long> hotelCount = new HashMap<>();
        hotelCount.put("hotel_001", 3L);
        hotelCount.put("hotel_002", 2L);
        
        UserBehaviorAnalytics analytics = new UserBehaviorAnalytics(
            "user_001",
            destinations,
            searchCount,
            hotelCount,
            200.0,
            4.3,
            "Double",
            8L,
            2L,
            "2024-01-15T10:30:00"
        );
        
        assertNotNull(analytics);
        assertEquals("user_001", analytics.getUserId());
        assertEquals(2, analytics.getPreferredDestinations().size());
        assertTrue(analytics.getPreferredDestinations().contains("Paris"));
        assertTrue(analytics.getPreferredDestinations().contains("London"));
        assertEquals(200.0, analytics.getAveragePriceBudget(), 0.01);
        assertEquals(4.3, analytics.getPreferredRating(), 0.01);
        assertEquals("Double", analytics.getPreferredRoomType());
        assertEquals(8L, analytics.getTotalSearches().longValue());
        assertEquals(2L, analytics.getTotalBookings().longValue());
        assertEquals(25.0, analytics.getConversionRate(), 0.01); // 2/8 * 100 = 25%
    }
    
    @Test
    public void testBookingEventValidation() {
        BookingEvent event = new BookingEvent();
        event.setUserId("user_test");
        event.setHotelId("hotel_test");
        event.setEventType("BOOK");
        event.setDestination("Tokyo");
        event.setPrice(300.0);
        event.setRating(4.8);
        
        assertEquals("user_test", event.getUserId());
        assertEquals("hotel_test", event.getHotelId());
        assertEquals("BOOK", event.getEventType());
        assertEquals("Tokyo", event.getDestination());
        assertEquals(300.0, event.getPrice(), 0.01);
        assertEquals(4.8, event.getRating(), 0.01);
    }
    
    @Test
    public void testSuggestionConfidenceScore() {
        List<String> hotels = Arrays.asList("hotel_001");
        
        BookingSuggestion suggestion = new BookingSuggestion(
            "user_001",
            "London",
            hotels,
            150.0,
            4.0,
            "Single",
            "Test suggestion",
            65.0
        );
        
        assertTrue(suggestion.getConfidenceScore() >= 0.0);
        assertTrue(suggestion.getConfidenceScore() <= 100.0);
        assertEquals(65.0, suggestion.getConfidenceScore(), 0.01);
    }
} 