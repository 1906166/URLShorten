package code.practice.URLShorten.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RedisCacheService {
    private static final int CACHE_CAPACITY = 5; // Low capacity to easily trigger eviction in frontend
    private static final long REDIS_LATENCY_MS = 3;

    private final Map<String, String> cache = new LinkedHashMap<String, String>(CACHE_CAPACITY, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            if (size() > CACHE_CAPACITY) {
                lastEvictedKey = eldest.getKey();
                return true;
            }
            return false;
        };
    };

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private String lastEvictedKey = null;

    public synchronized Optional<String> get(String shortKey) {
        simulateLatency();
        String value = cache.get(shortKey);
        if (value != null) {
            hits.incrementAndGet();
            return Optional.of(value);
        } else {
            misses.incrementAndGet();
            return Optional.empty();
        }
    }

    public synchronized void put(String shortKey, String longUrl) {
        simulateLatency();
        cache.put(shortKey, longUrl);
    }

    public synchronized void evict(String shortKey) {
        cache.remove(shortKey);
    }

    public synchronized Map<String, String> getCacheContents() {
        return new LinkedHashMap<>(cache);
    }

    public long getHits() {
        return hits.get();
    }

    public long getMisses() {
        return misses.get();
    }

    public String getLastEvictedKey() {
        String key = lastEvictedKey;
        lastEvictedKey = null; // Consume the eviction message
        return key;
    }

    public synchronized void clear() {
        cache.clear();
        hits.set(0);
        misses.set(0);
        lastEvictedKey = null;
    }

    private void simulateLatency() {
        try {
            Thread.sleep(REDIS_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
