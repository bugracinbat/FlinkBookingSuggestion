package com.flinkbooking.aggregators;

import com.flinkbooking.model.BookingEvent;
import org.apache.flink.api.common.functions.AggregateFunction;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregator to count events by type for analytics windows
 */
public class EventCountAggregator implements AggregateFunction<BookingEvent, EventCountAggregator.EventSummary, EventCountAggregator.EventSummary> {

    public static class EventSummary {
        public long totalEvents = 0;
        public Map<String, Long> eventTypeCounts = new HashMap<>();
        
        public EventSummary() {}
        
        public EventSummary(long totalEvents, Map<String, Long> eventTypeCounts) {
            this.totalEvents = totalEvents;
            this.eventTypeCounts = eventTypeCounts;
        }
    }

    @Override
    public EventSummary createAccumulator() {
        return new EventSummary();
    }

    @Override
    public EventSummary add(BookingEvent event, EventSummary accumulator) {
        accumulator.totalEvents++;
        accumulator.eventTypeCounts.merge(event.getEventType(), 1L, Long::sum);
        return accumulator;
    }

    @Override
    public EventSummary getResult(EventSummary accumulator) {
        return accumulator;
    }

    @Override
    public EventSummary merge(EventSummary acc1, EventSummary acc2) {
        EventSummary merged = new EventSummary();
        merged.totalEvents = acc1.totalEvents + acc2.totalEvents;
        
        // Merge event type counts
        merged.eventTypeCounts.putAll(acc1.eventTypeCounts);
        acc2.eventTypeCounts.forEach((key, value) -> 
            merged.eventTypeCounts.merge(key, value, Long::sum));
        
        return merged;
    }
} 