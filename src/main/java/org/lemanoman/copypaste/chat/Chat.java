package org.lemanoman.copypaste.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * A chat is identified by a short shareable code. Messages posted under the
 * same code are visible to anyone who has that code.
 */
@Entity
@Table(name = "chats", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 16)
    private String code;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastActivityAt;

    protected Chat() {
        // JPA
    }

    public Chat(String code, Instant createdAt) {
        this.code = code;
        this.createdAt = createdAt;
        this.lastActivityAt = createdAt;
    }

    public void touchActivity(Instant now) {
        this.lastActivityAt = now;
    }

    public boolean isExpired(Instant now, java.time.Duration ttl) {
        return lastActivityAt.plus(ttl).isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }
}
