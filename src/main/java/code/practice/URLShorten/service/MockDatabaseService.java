package code.practice.URLShorten.service;

import code.practice.URLShorten.model.UrlMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockDatabaseService {
    // Simulating Cassandra/DynamoDB primary key lookup
    private final Map<String, UrlMetadata> database = new ConcurrentHashMap<>();
    
    // Configurable simulated latency (in milliseconds) to reflect DB lookups
    private static final long DB_LATENCY_MS = 80;

    public void save(UrlMetadata metadata) {
        simulateLatency();
        database.put(metadata.getShortKey(), metadata);
    }

    public Optional<UrlMetadata> findById(String shortKey) {
        simulateLatency();
        return Optional.ofNullable(database.get(shortKey));
    }

    public List<UrlMetadata> findAll() {
        simulateLatency();
        return new ArrayList<>(database.values());
    }

    public void incrementClicks(String shortKey) {
        simulateLatency();
        UrlMetadata metadata = database.get(shortKey);
        if (metadata != null) {
            metadata.incrementClicks();
        }
    }

    public int getRecordCount() {
        return database.size();
    }

    public void clear() {
        database.clear();
    }

    private void simulateLatency() {
        try {
            Thread.sleep(DB_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
