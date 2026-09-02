package org.lemanoman.copypaste.chat;

import org.lemanoman.copypaste.common.CodeGenerator;
import org.lemanoman.copypaste.common.exception.ChatNotFoundException;
import org.lemanoman.copypaste.config.ChatProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final CodeGenerator codeGenerator;
    private final ChatProperties chatProperties;
    private final Clock clock;

    @Autowired
    public ChatService(ChatRepository chatRepository, CodeGenerator codeGenerator, ChatProperties chatProperties) {
        this(chatRepository, codeGenerator, chatProperties, Clock.systemUTC());
    }

    ChatService(ChatRepository chatRepository, CodeGenerator codeGenerator, ChatProperties chatProperties, Clock clock) {
        this.chatRepository = chatRepository;
        this.codeGenerator = codeGenerator;
        this.chatProperties = chatProperties;
        this.clock = clock;
    }

    @Transactional
    public Chat createChat() {
        String code = codeGenerator.generateUniqueCode();
        Chat chat = new Chat(code, Instant.now(clock));
        return chatRepository.save(chat);
    }

    /**
     * Returns the chat for the given code, normalizing case, or throws if it
     * doesn't exist or has expired (an expired-but-not-yet-swept chat is
     * treated as not found).
     */
    @Transactional(readOnly = true)
    public Chat getActiveChat(String code) {
        Chat chat = chatRepository.findByCode(normalize(code))
                .orElseThrow(() -> new ChatNotFoundException(code));
        if (chat.isExpired(Instant.now(clock), chatProperties.getTtl())) {
            throw new ChatNotFoundException(code);
        }
        return chat;
    }

    @Transactional
    public void touchActivity(Chat chat) {
        chat.touchActivity(Instant.now(clock));
    }

    public String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }
}
