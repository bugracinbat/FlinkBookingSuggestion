package com.flinkbooking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a booking event in the system
 */
public class BookingEvent implements Serializable {
    
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
        this.userId = userId;
        this.hotelId = hotelId;
        this.eventType = eventType;
        this.destination = destination;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.price = price;
        this.rating = rating;
        this.roomType = roomType;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.bookingId = generateBookingId();
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
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
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