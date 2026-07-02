package code.practice.URLShorten.service;

import code.practice.URLShorten.model.KafkaEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsService {
    private final AtomicLong totalShortenedCount = new AtomicLong(0);
    private final AtomicLong totalRedirectCount = new AtomicLong(0);
    private final Map<String, AtomicLong> clicksByUrl = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> clicksByReferer = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> clicksByDevice = new ConcurrentHashMap<>();

    public AnalyticsService() {
        // Initialize with some mock data for visual aesthetics
        clicksByReferer.put("Direct / Email", new AtomicLong(120));
        clicksByReferer.put("Twitter / X", new AtomicLong(345));
        clicksByReferer.put("LinkedIn", new AtomicLong(189));
        clicksByReferer.put("Google", new AtomicLong(412));
        clicksByReferer.put("GitHub", new AtomicLong(87));

        clicksByDevice.put("Mobile", new AtomicLong(642));
        clicksByDevice.put("Desktop", new AtomicLong(458));
        clicksByDevice.put("Tablet", new AtomicLong(53));

        totalRedirectCount.set(1153);
        totalShortenedCount.set(42);
    }

    public void consume(KafkaEvent event) {
        if ("URL_SHORTENED".equals(event.getType())) {
            totalShortenedCount.incrementAndGet();
        } else if ("URL_REDIRECTED".equals(event.getType())) {
            totalRedirectCount.incrementAndGet();
            String shortKey = event.getDetails().substring(event.getDetails().lastIndexOf(":") + 2).trim();
            clicksByUrl.computeIfAbsent(shortKey, k -> new AtomicLong(0)).incrementAndGet();
            
            // Randomly simulate referers & devices for new incoming requests
            String[] referers = {"Twitter / X", "LinkedIn", "Google", "Direct / Email", "GitHub"};
            String randomReferer = referers[(int) (Math.random() * referers.length)];
            clicksByReferer.computeIfAbsent(randomReferer, k -> new AtomicLong(0)).incrementAndGet();

            String[] devices = {"Mobile", "Desktop", "Tablet"};
            String randomDevice = devices[(int) (Math.random() * devices.length)];
            clicksByDevice.computeIfAbsent(randomDevice, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalShortened", totalShortenedCount.get());
        summary.put("totalRedirects", totalRedirectCount.get());

        Map<String, Long> urlClicks = new HashMap<>();
        clicksByUrl.forEach((k, v) -> urlClicks.put(k, v.get()));
        summary.put("clicksByUrl", urlClicks);

        Map<String, Long> refererClicks = new HashMap<>();
        clicksByReferer.forEach((k, v) -> refererClicks.put(k, v.get()));
        summary.put("clicksByReferer", refererClicks);

        Map<String, Long> deviceClicks = new HashMap<>();
        clicksByDevice.forEach((k, v) -> deviceClicks.put(k, v.get()));
        summary.put("clicksByDevice", deviceClicks);

        return summary;
    }

    public void clear() {
        totalShortenedCount.set(0);
        totalRedirectCount.set(0);
        clicksByUrl.clear();
        clicksByReferer.clear();
        clicksByDevice.clear();

        // Seed fresh empty structures
        clicksByReferer.put("Direct / Email", new AtomicLong(0));
        clicksByReferer.put("Twitter / X", new AtomicLong(0));
        clicksByReferer.put("LinkedIn", new AtomicLong(0));
        clicksByReferer.put("Google", new AtomicLong(0));
        clicksByReferer.put("GitHub", new AtomicLong(0));

        clicksByDevice.put("Mobile", new AtomicLong(0));
        clicksByDevice.put("Desktop", new AtomicLong(0));
        clicksByDevice.put("Tablet", new AtomicLong(0));
    }
}
