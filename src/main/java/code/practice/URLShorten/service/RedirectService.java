package code.practice.URLShorten.service;

import code.practice.URLShorten.model.RedirectResult;
import code.practice.URLShorten.model.UrlMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RedirectService {

    private final RedisCacheService cacheService;
    private final UrlMetadataService urlMetadataService;
    private final MockKafkaService kafkaService;

    @Autowired
    public RedirectService(RedisCacheService cacheService, 
                           UrlMetadataService urlMetadataService, 
                           MockKafkaService kafkaService) {
        this.cacheService = cacheService;
        this.urlMetadataService = urlMetadataService;
        this.kafkaService = kafkaService;
    }

    public Optional<RedirectResult> handleRedirect(String shortKey) {
        long startTime = System.currentTimeMillis();
        List<String> traceSteps = new ArrayList<>();
        traceSteps.add("API Gateway routed request to Redirect Service");

        // 1. Check Redis Cache
        traceSteps.add("Querying Redis Cache Cluster for key: " + shortKey);
        Optional<String> cachedUrl = cacheService.get(shortKey);
        
        boolean cacheHit = cachedUrl.isPresent();
        String longUrl = null;

        if (cacheHit) {
            longUrl = cachedUrl.get();
            traceSteps.add("Redis CACHE HIT! Found mapping: " + shortKey + " -> " + longUrl);
            
            // Asynchronously register hit in DB (eventual consistency)
            new Thread(() -> {
                try {
                    // Simulate async database network call
                    Thread.sleep(20);
                    urlMetadataService.getUrl(shortKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else {
            traceSteps.add("Redis CACHE MISS! Key not found in cache");
            
            // 2. Query Metadata Service (Database Lookup)
            traceSteps.add("Forwarding request to URL Metadata Service");
            Optional<UrlMetadata> dbMetadata = urlMetadataService.getUrl(shortKey);
            
            if (dbMetadata.isPresent()) {
                longUrl = dbMetadata.get().getLongUrl();
                traceSteps.add("URL Metadata Service retrieved record from Distributed DB (Cassandra/DynamoDB)");
                
                // 3. Populate Redis Cache
                traceSteps.add("Writing mapping back to Redis Cache Cluster: " + shortKey + " -> " + longUrl);
                cacheService.put(shortKey, longUrl);
            } else {
                traceSteps.add("URL Metadata Service found no record in Database for key: " + shortKey);
            }
        }

        if (longUrl != null) {
            // 4. Publish Event to Kafka
            traceSteps.add("Publishing redirect event to Kafka Broker queue");
            kafkaService.publish("URL_REDIRECTED", "Redirected key: " + shortKey);
            
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            traceSteps.add("Redirect completed successfully in " + elapsed + " ms");
            
            return Optional.of(new RedirectResult(longUrl, cacheHit, elapsed, traceSteps));
        }

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        traceSteps.add("Redirect failed: key not found. Elapsed time: " + elapsed + " ms");
        return Optional.empty();
    }
}
