package code.practice.URLShorten.service;

import code.practice.URLShorten.model.UrlMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShortenService {

    private final UrlMetadataService urlMetadataService;

    @Autowired
    public ShortenService(UrlMetadataService urlMetadataService) {
        this.urlMetadataService = urlMetadataService;
    }

    public UrlMetadata shortenUrl(String longUrl, int expirationDays) {
        // Validation (basic)
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Long URL cannot be empty");
        }
        
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "https://" + longUrl;
        }

        return urlMetadataService.createShortUrl(longUrl, expirationDays);
    }
}
