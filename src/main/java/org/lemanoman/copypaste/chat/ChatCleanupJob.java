package org.lemanoman.copypaste.chat;

import org.lemanoman.copypaste.config.ChatProperties;
import org.lemanoman.copypaste.message.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Periodically removes chats (and their messages, via cascade at the DB
 * level through the FK) that have been inactive for longer than the
 * configured TTL.
 */
@Component
public class ChatCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ChatCleanupJob.class);

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatProperties chatProperties;

    public ChatCleanupJob(ChatRepository chatRepository, MessageRepository messageRepository, ChatProperties chatProperties) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.chatProperties = chatProperties;
    }

    @Scheduled(fixedRateString = "${copypaste.chat.cleanup-interval}")
    @Transactional
    public void purgeExpiredChats() {
        Instant threshold = Instant.now().minus(chatProperties.getTtl());
        List<Chat> expired = chatRepository.findByLastActivityAtBefore(threshold);
        for (Chat chat : expired) {
            messageRepository.deleteByChat(chat);
            chatRepository.delete(chat);
        }
        if (!expired.isEmpty()) {
            log.info("Purged {} expired chat(s)", expired.size());
        }
    }
}
