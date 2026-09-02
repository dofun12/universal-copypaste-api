package org.lemanoman.copypaste.common;

import org.lemanoman.copypaste.chat.ChatRepository;
import org.lemanoman.copypaste.config.ChatProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates short, human-friendly, uppercase-letter chat codes (e.g. "AYBDC")
 * and retries on the rare collision with an existing code.
 */
@Component
public class CodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int MAX_ATTEMPTS = 20;

    private final SecureRandom random = new SecureRandom();
    private final ChatRepository chatRepository;
    private final ChatProperties chatProperties;

    public CodeGenerator(ChatRepository chatRepository, ChatProperties chatProperties) {
        this.chatRepository = chatRepository;
        this.chatProperties = chatProperties;
    }

    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = randomCode(chatProperties.getCodeLength());
            if (!chatRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique chat code after " + MAX_ATTEMPTS + " attempts");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
