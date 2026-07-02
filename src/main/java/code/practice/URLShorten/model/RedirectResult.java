package code.practice.URLShorten.model;

import java.util.List;

public class RedirectResult {
    private String longUrl;
    private boolean cacheHit;
    private long latencyMs;
    private List<String> traceSteps;

    public RedirectResult() {
    }

    public RedirectResult(String longUrl, boolean cacheHit, long latencyMs, List<String> traceSteps) {
        this.longUrl = longUrl;
        this.cacheHit = cacheHit;
        this.latencyMs = latencyMs;
        this.traceSteps = traceSteps;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public List<String> getTraceSteps() {
        return traceSteps;
    }

    public void setTraceSteps(List<String> traceSteps) {
        this.traceSteps = traceSteps;
    }
}
