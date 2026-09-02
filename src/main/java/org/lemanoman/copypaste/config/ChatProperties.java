package org.lemanoman.copypaste.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Binds the {@code copypaste.chat.*} properties from application.yml.
 */
@ConfigurationProperties(prefix = "copypaste.chat")
public class ChatProperties {

    /** How long a chat may stay inactive before it becomes eligible for cleanup. */
    private Duration ttl = Duration.ofHours(24);

    /** Number of uppercase letters used when generating a chat code (e.g. "AYBDC" = 5). */
    private int codeLength = 5;

    /** How often the expiry sweep job runs. */
    private Duration cleanupInterval = Duration.ofMinutes(15);

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
}
