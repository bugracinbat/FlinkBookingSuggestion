# Flink Booking Suggestion Application

A real-time booking suggestion system built with Apache Flink that analyzes user behavior patterns to generate personalized hotel booking recommendations.

## Overview

This application processes streaming booking events and generates intelligent suggestions based on user behavior analytics. It uses Apache Flink's streaming capabilities to provide real-time recommendations with high throughput and low latency.

## Features

- **Real-time Event Processing**: Processes booking events (SEARCH, VIEW, BOOK, CANCEL) in real-time
- **User Behavior Analytics**: Tracks user preferences, destinations, price ranges, and booking patterns
- **Personalized Suggestions**: Generates customized recommendations based on individual user behavior
- **Confidence Scoring**: Each suggestion includes a confidence score indicating recommendation quality
- **Windowed Analytics**: Provides aggregate analytics over time windows
- **Stateful Processing**: Maintains user state across multiple events for personalized experiences

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Booking Events │───▶│  Flink Stream   │───▶│   Suggestions   │
│     Source      │    │   Processing    │    │     Output      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │  User Behavior  │
                       │    Analytics    │
                       └─────────────────┘
```

## Data Models

### BookingEvent

- `userId`: User identifier
- `hotelId`: Hotel identifier
- `eventType`: Type of event (SEARCH, VIEW, BOOK, CANCEL)
- `destination`: Destination city/location
- `checkInDate` / `checkOutDate`: Booking dates
- `price`: Hotel price
- `rating`: Hotel rating
- `roomType`: Type of room

### BookingSuggestion

- `userId`: Target user for the suggestion
- `destination`: Recommended destination
- `recommendedHotels`: List of recommended hotels
- `averagePrice`: Average price based on user preferences
- `averageRating`: Average rating based on user preferences
- `suggestionReason`: Explanation for the recommendation
- `confidenceScore`: Confidence level (0-100)

## Getting Started

### Prerequisites

- Java 11 or higher
- Apache Maven 3.6+
- Apache Flink 1.18.0 (optional, for cluster deployment)

### Building the Application

```bash
mvn clean package
```

### Running Locally

```bash
# Run the application
mvn exec:java -Dexec.mainClass="com.flinkbooking.BookingSuggestionApp"

# Or run the built JAR
java -jar target/booking-suggestion-1.0.0.jar
```

### Running on Flink Cluster

```bash
# Submit to Flink cluster
flink run target/booking-suggestion-1.0.0.jar
```

## Configuration

The application can be configured through `src/main/resources/application.properties`:

- **Parallelism**: Number of parallel tasks
- **Checkpoint Interval**: How often to checkpoint state
- **Event Rates**: Different rates for different event types
- **Suggestion Thresholds**: Minimum events required for suggestions
- **Window Sizes**: Analytics window configurations

## Monitoring

The application provides several monitoring outputs:

1. **EVENTS**: Raw booking events as they arrive
2. **SUGGESTIONS**: Generated suggestions with full details
3. **ANALYTICS**: Windowed analytics summary every 30 seconds
4. **EVENT_TYPES**: Event type distribution every 20 seconds

## Sample Output

### Booking Event

```
BookingEvent{userId='user_001', hotelId='hotel_003', eventType='SEARCH',
destination='Paris', price=245.67, rating=4.2}
```

### Generated Suggestion

```
BookingSuggestion{userId='user_001', destination='Paris',
recommendedHotels=[hotel_003, hotel_007, hotel_001], averagePrice=234.56,
averageRating=4.3, suggestionReason='Based on your recent activity, we suggest
exploring Paris with these highly-rated options.', confidenceScore=75.0}
```

## Algorithm Details

### Suggestion Generation Logic

1. **Minimum Activity Threshold**: User must have at least 3 events before suggestions are generated
2. **Frequency Control**: Suggestions are generated every 5th event to avoid spam
3. **Preference Learning**: System learns user preferences for:
   - Destinations (most searched locations)
   - Price range (average of viewed prices)
   - Hotel ratings (average of viewed ratings)
   - Room types (most recent selections)

### Confidence Scoring

The confidence score (0-100) is calculated based on:

- **Activity Level** (40 points max): More user activity = higher confidence
- **Conversion Rate** (30 points max): Users who book more get higher confidence
- **Preference Consistency** (20 points max): Focused preferences = higher confidence
- **Recent Activity** (10 points): Recent activity bonus

## Extending the Application

### Adding New Event Types

1. Update the `eventTypes` list in `BookingEventSource`
2. Add handling logic in `BookingSuggestionProcessor.updateUserBehavior()`
3. Update suggestion generation logic if needed

### Adding External Data Sources

1. Implement `SourceFunction<BookingEvent>` for your data source
2. Replace `BookingEventSource` with your implementation
3. Update configuration as needed

### Adding Sinks

1. Implement sink functions for your target systems (Kafka, Elasticsearch, etc.)
2. Add sink configuration to the main application
3. Connect the suggestion stream to your sinks

## Testing

Run the test suite:

```bash
mvn test
```

## Performance Tuning

- **Parallelism**: Adjust based on your cluster size and throughput requirements
- **Checkpoint Interval**: Balance between performance and fault tolerance
- **State Backend**: Consider RocksDB for large state or high throughput
- **Memory Configuration**: Tune JVM heap and Flink memory settings

## Deployment

### Kubernetes

The application can be deployed on Kubernetes using Flink Kubernetes Operator.

### Docker

Build a Docker image:

```bash
docker build -t booking-suggestion-app .
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions and support, please open an issue in the GitHub repository.
