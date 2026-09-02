package org.lemanoman.copypaste.chat;

import java.time.Instant;

/**
 * API representation of a chat's metadata (never exposes internal DB id).
 */
public record ChatDto(String code, Instant createdAt) {

    public static ChatDto from(Chat chat) {
        return new ChatDto(chat.getCode(), chat.getCreatedAt());
    }
}
