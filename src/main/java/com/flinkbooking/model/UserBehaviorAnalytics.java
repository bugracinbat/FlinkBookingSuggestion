package com.flinkbooking.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Represents user behavior analytics for generating personalized suggestions
 */
public class UserBehaviorAnalytics implements Serializable {
    
    private String userId;
    private Set<String> preferredDestinations;
    private Map<String, Long> destinationSearchCount;
    private Map<String, Long> hotelViewCount;
    private Double averagePriceBudget;
    private Double preferredRating;
    private String preferredRoomType;
    private Long totalSearches;
    private Long totalBookings;
    private String lastActivity;
    private Double conversionRate;
    
    // Default constructor
    public UserBehaviorAnalytics() {}
    
    // Constructor
    public UserBehaviorAnalytics(String userId, Set<String> preferredDestinations,
                               Map<String, Long> destinationSearchCount,
                               Map<String, Long> hotelViewCount,
                               Double averagePriceBudget, Double preferredRating,
                               String preferredRoomType, Long totalSearches,
                               Long totalBookings, String lastActivity) {
        this.userId = userId;
        this.preferredDestinations = preferredDestinations;
        this.destinationSearchCount = destinationSearchCount;
        this.hotelViewCount = hotelViewCount;
        this.averagePriceBudget = averagePriceBudget;
        this.preferredRating = preferredRating;
        this.preferredRoomType = preferredRoomType;
        this.totalSearches = totalSearches;
        this.totalBookings = totalBookings;
        this.lastActivity = lastActivity;
        this.conversionRate = calculateConversionRate();
    }
    
    private Double calculateConversionRate() {
        if (totalSearches == 0) return 0.0;
        return (double) totalBookings / totalSearches * 100;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Set<String> getPreferredDestinations() { return preferredDestinations; }
    public void setPreferredDestinations(Set<String> preferredDestinations) { 
        this.preferredDestinations = preferredDestinations; 
    }
    
    public Map<String, Long> getDestinationSearchCount() { return destinationSearchCount; }
    public void setDestinationSearchCount(Map<String, Long> destinationSearchCount) { 
        this.destinationSearchCount = destinationSearchCount; 
    }
    
    public Map<String, Long> getHotelViewCount() { return hotelViewCount; }
    public void setHotelViewCount(Map<String, Long> hotelViewCount) { 
        this.hotelViewCount = hotelViewCount; 
    }
    
    public Double getAveragePriceBudget() { return averagePriceBudget; }
    public void setAveragePriceBudget(Double averagePriceBudget) { this.averagePriceBudget = averagePriceBudget; }
    
    public Double getPreferredRating() { return preferredRating; }
    public void setPreferredRating(Double preferredRating) { this.preferredRating = preferredRating; }
    
    public String getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(String preferredRoomType) { this.preferredRoomType = preferredRoomType; }
    
    public Long getTotalSearches() { return totalSearches; }
    public void setTotalSearches(Long totalSearches) { this.totalSearches = totalSearches; }
    
    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }
    
    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }
    
    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    
    @Override
    public String toString() {
        return "UserBehaviorAnalytics{" +
                "userId='" + userId + '\'' +
                ", preferredDestinations=" + preferredDestinations +
                ", averagePriceBudget=" + averagePriceBudget +
                ", preferredRating=" + preferredRating +
                ", preferredRoomType='" + preferredRoomType + '\'' +
                ", totalSearches=" + totalSearches +
                ", totalBookings=" + totalBookings +
                ", conversionRate=" + conversionRate +
                '}';
    }
} 