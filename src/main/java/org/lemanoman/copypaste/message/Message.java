package org.lemanoman.copypaste.message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.lemanoman.copypaste.chat.Chat;

import java.time.Instant;

/**
 * A single message posted to a chat. {@code renderedContent} is a pre-escaped
 * HTML fragment with plain URLs turned into links and image URLs embedded as
 * {@code <img>} tags, so the frontend can render it directly.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    @Column(nullable = false)
    private String renderedContent;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Message() {
        // JPA
    }

    public Message(Chat chat, String content, String renderedContent, Instant createdAt) {
        this.chat = chat;
        this.content = content;
        this.renderedContent = renderedContent;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public String getContent() {
        return content;
    }

    public String getRenderedContent() {
        return renderedContent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
