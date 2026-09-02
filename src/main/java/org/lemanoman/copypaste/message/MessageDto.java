package org.lemanoman.copypaste.message;

import java.time.Instant;

public record MessageDto(Long id, String content, String renderedContent, Instant createdAt) {

    public static MessageDto from(Message message) {
        return new MessageDto(message.getId(), message.getContent(), message.getRenderedContent(), message.getCreatedAt());
    }
}
