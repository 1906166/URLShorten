package code.practice.URLShorten.model;

import java.time.LocalDateTime;

public class UrlMetadata {
    private String shortKey;
    private String longUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long clicks;

    public UrlMetadata() {
    }

    public UrlMetadata(String shortKey, String longUrl, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.shortKey = shortKey;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clicks = 0;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public void incrementClicks() {
        this.clicks++;
    }
}
