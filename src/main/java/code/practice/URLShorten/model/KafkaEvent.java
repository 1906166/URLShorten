package code.practice.URLShorten.model;

import java.time.LocalDateTime;

public class KafkaEvent {
    private String id;
    private String type;
    private LocalDateTime timestamp;
    private String details;

    public KafkaEvent() {
    }

    public KafkaEvent(String id, String type, LocalDateTime timestamp, String details) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
