package code.practice.URLShorten.service;

import code.practice.URLShorten.model.UrlMetadata;
import code.practice.URLShorten.repository.UrlMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlMetadataService {

    private final UrlMetadataRepository urlMetadataRepository;
    private final IdGeneratorService idGeneratorService;
    private final MockKafkaService kafkaService;

    @Autowired
    public UrlMetadataService(UrlMetadataRepository urlMetadataRepository, 
                              IdGeneratorService idGeneratorService, 
                              MockKafkaService kafkaService) {
        this.urlMetadataRepository = urlMetadataRepository;
        this.idGeneratorService = idGeneratorService;
        this.kafkaService = kafkaService;
    }

    public UrlMetadata createShortUrl(String longUrl, int expirationDays) {
        // Generate globally unique ID
        long id = idGeneratorService.nextId();
        
        // Encode ID using Base62
        String shortKey = idGeneratorService.encode(id);
        
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusDays(expirationDays);
        
        UrlMetadata metadata = new UrlMetadata(shortKey, longUrl, createdAt, expiresAt);
        
        // Persist to MongoDB database
        urlMetadataRepository.save(metadata);
        
        // Emit Kafka event
        kafkaService.publish("URL_SHORTENED", "Created short URL mapping for long URL: " + longUrl + " -> " + shortKey);
        
        return metadata;
    }

    public Optional<UrlMetadata> getUrl(String shortKey) {
        // Lookup from database
        Optional<UrlMetadata> optionalMetadata = urlMetadataRepository.findById(shortKey);
        
        if (optionalMetadata.isPresent()) {
            // Async trigger database update click counter (reflecting async write or eventual consistency)
            incrementClicks(shortKey);
        }
        
        return optionalMetadata;
    }

    public void incrementClicks(String shortKey) {
        urlMetadataRepository.findById(shortKey).ifPresent(metadata -> {
            metadata.incrementClicks();
            urlMetadataRepository.save(metadata);
        });
    }
}
