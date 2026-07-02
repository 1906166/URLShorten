package code.practice.URLShorten.controller;

import code.practice.URLShorten.model.RedirectResult;
import code.practice.URLShorten.model.UrlMetadata;
import code.practice.URLShorten.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class UrlShortenerApiController {

    private final ShortenService shortenService;
    private final RedirectService redirectService;
    private final RedisCacheService cacheService;
    private final MockDatabaseService databaseService;
    private final MockKafkaService kafkaService;
    private final AnalyticsService analyticsService;

    @Autowired
    public UrlShortenerApiController(ShortenService shortenService,
                                     RedirectService redirectService,
                                     RedisCacheService cacheService,
                                     MockDatabaseService databaseService,
                                     MockKafkaService kafkaService,
                                     AnalyticsService analyticsService) {
        this.shortenService = shortenService;
        this.redirectService = redirectService;
        this.cacheService = cacheService;
        this.databaseService = databaseService;
        this.kafkaService = kafkaService;
        this.analyticsService = analyticsService;
    }

    // 1. Shorten URL Endpoint
    @PostMapping("/api/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, Object> request) {
        try {
            String longUrl = (String) request.get("longUrl");
            int expirationDays = request.containsKey("expirationDays") ? 
                    Integer.parseInt(request.get("expirationDays").toString()) : 7;
            
            UrlMetadata metadata = shortenService.shortenUrl(longUrl, expirationDays);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // 2. Resolve URL Endpoint (Returns JSON trace for frontend visualization)
    @GetMapping("/api/resolve/{shortKey}")
    public ResponseEntity<?> resolve(@PathVariable String shortKey) {
        Optional<RedirectResult> result = redirectService.handleRedirect(shortKey);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> err = new HashMap<>();
            err.put("error", "URL Key not found or has expired");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
    }

    // 3. Real Redirection Endpoint (Performs actual 302 redirect)
    @GetMapping("/r/{shortKey}")
    public ResponseEntity<?> redirect(@PathVariable String shortKey) {
        Optional<RedirectResult> result = redirectService.handleRedirect(shortKey);
        if (result.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(result.get().getLongUrl()))
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<h1>404 URL Not Found</h1><p>The shortened URL key '" + shortKey + "' does not exist.</p>");
        }
    }

    // 4. System State Inspection Endpoint
    @GetMapping("/api/system/state")
    public ResponseEntity<?> getSystemState() {
        Map<String, Object> state = new HashMap<>();
        
        // Cache info
        Map<String, Object> cacheInfo = new HashMap<>();
        cacheInfo.put("contents", cacheService.getCacheContents());
        cacheInfo.put("hits", cacheService.getHits());
        cacheInfo.put("misses", cacheService.getMisses());
        cacheInfo.put("lastEvicted", cacheService.getLastEvictedKey());
        state.put("cache", cacheInfo);
        
        // DB info
        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("records", databaseService.findAll());
        dbInfo.put("recordCount", databaseService.getRecordCount());
        state.put("database", dbInfo);
        
        // Kafka event log
        state.put("kafkaEvents", kafkaService.getRecentEvents());
        
        // Analytics
        state.put("analytics", analyticsService.getMetricsSummary());
        
        return ResponseEntity.ok(state);
    }

    // 5. System Reset Endpoint
    @PostMapping("/api/system/clear")
    public ResponseEntity<?> clearSystem() {
        cacheService.clear();
        databaseService.clear();
        kafkaService.clear();
        analyticsService.clear();
        
        Map<String, String> msg = new HashMap<>();
        msg.put("message", "System state has been reset successfully");
        return ResponseEntity.ok(msg);
    }
}
