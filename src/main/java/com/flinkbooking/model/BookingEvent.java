package com.flinkbooking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a booking event in the system
 */
public class BookingEvent implements Serializable {
    
    private static final Set<String> VALID_EVENT_TYPES = new HashSet<String>() {{
        add("SEARCH");
        add("VIEW");
        add("BOOK");
        add("CANCEL");
    }};
    
    private String userId;
    private String hotelId;
    private String bookingId;
    private String eventType; // SEARCH, VIEW, BOOK, CANCEL
    private String timestamp;
    private String destination;
    private String checkInDate;
    private String checkOutDate;
    private Double price;
    private Double rating;
    private String roomType;
    
    // Default constructor
    public BookingEvent() {}
    
    // Constructor
    public BookingEvent(String userId, String hotelId, String eventType, 
                       String destination, String checkInDate, String checkOutDate, 
                       Double price, Double rating, String roomType) {
        this.userId = validateUserId(userId);
        this.hotelId = validateHotelId(hotelId);
        this.eventType = validateEventType(eventType);
        this.destination = validateDestination(destination);
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.price = validatePrice(price);
        this.rating = validateRating(rating);
        this.roomType = roomType;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.bookingId = generateBookingId();
    }
    
    private String validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return userId;
    }
    
    private String validateHotelId(String hotelId) {
        if (hotelId == null || hotelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel ID cannot be null or empty");
        }
        return hotelId;
    }
    
    private String validateEventType(String eventType) {
        if (eventType == null || !VALID_EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException("Event type must be one of: " + VALID_EVENT_TYPES);
        }
        return eventType;
    }
    
    private String validateDestination(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be null or empty");
        }
        return destination;
    }
    
    private Double validatePrice(Double price) {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        return price;
    }
    
    private Double validateRating(Double rating) {
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        return rating;
    }

    private String generateBookingId() {
        return "BK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = validateEventType(eventType); }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = validatePrice(price); }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = validateRating(rating); }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    @Override
    public String toString() {
        return "BookingEvent{" +
                "userId='" + userId + '\'' +
                ", hotelId='" + hotelId + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }
} 