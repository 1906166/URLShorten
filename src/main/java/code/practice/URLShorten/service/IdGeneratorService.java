package code.practice.URLShorten.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class IdGeneratorService {
    // Starting with a large counter offset to yield 7-character Base62 keys
    private final AtomicLong counter = new AtomicLong(100018619L);

    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_ALPHABET.length();
    private static final long GENERATOR_LATENCY_MS = 5;

    public long nextId() {
        simulateLatency();
        return counter.incrementAndGet();
    }

    public String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62_ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    public long decode(String shortKey) {
        long id = 0;
        for (int i = 0; i < shortKey.length(); i++) {
            char c = shortKey.charAt(i);
            int value = BASE62_ALPHABET.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            id = id * BASE + value;
        }
        return id;
    }

    private void simulateLatency() {
        try {
            Thread.sleep(GENERATOR_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
