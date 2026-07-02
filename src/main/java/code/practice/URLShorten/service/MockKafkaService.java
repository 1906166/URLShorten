package code.practice.URLShorten.service;

import code.practice.URLShorten.model.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MockKafkaService {
    private final ConcurrentLinkedQueue<KafkaEvent> eventLog = new ConcurrentLinkedQueue<>();
    private static final int MAX_LOG_SIZE = 50;

    private final AnalyticsService analyticsService;

    @Autowired
    public MockKafkaService(@Lazy AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public void publish(String eventType, String details) {
        KafkaEvent event = new KafkaEvent(
                UUID.randomUUID().toString(),
                eventType,
                LocalDateTime.now(),
                details
        );
        
        eventLog.add(event);
        while (eventLog.size() > MAX_LOG_SIZE) {
            eventLog.poll();
        }

        // Asynchronously or synchronously deliver to consumer (AnalyticsService)
        new Thread(() -> {
            try {
                // Simulate Kafka transmission/broker latency
                Thread.sleep(15);
                analyticsService.consume(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public List<KafkaEvent> getRecentEvents() {
        List<KafkaEvent> list = new ArrayList<>(eventLog);
        Collections.reverse(list); // Newest first
        return list;
    }

    public void clear() {
        eventLog.clear();
    }
}
