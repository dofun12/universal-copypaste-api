package org.lemanoman.copypaste.common.exception;

/**
 * Thrown when a chat code doesn't exist, or the chat has expired and been
 * cleaned up.
 */
public class ChatNotFoundException extends RuntimeException {

    public ChatNotFoundException(String code) {
        super("No active chat found for code '" + code + "'");
    }
}
