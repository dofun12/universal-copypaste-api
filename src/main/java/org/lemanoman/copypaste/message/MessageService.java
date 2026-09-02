package org.lemanoman.copypaste.message;

import org.lemanoman.copypaste.chat.Chat;
import org.lemanoman.copypaste.chat.ChatService;
import org.lemanoman.copypaste.common.ContentFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final ContentFormatter contentFormatter;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                           ChatService chatService,
                           ContentFormatter contentFormatter,
                           SimpMessagingTemplate messagingTemplate) {
        this(messageRepository, chatService, contentFormatter, messagingTemplate, Clock.systemUTC());
    }

    MessageService(MessageRepository messageRepository,
                    ChatService chatService,
                    ContentFormatter contentFormatter,
                    SimpMessagingTemplate messagingTemplate,
                    Clock clock) {
        this.messageRepository = messageRepository;
        this.chatService = chatService;
        this.contentFormatter = contentFormatter;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    @Transactional
    public MessageDto postMessage(String code, String rawContent) {
        Chat chat = chatService.getActiveChat(code);
        String rendered = contentFormatter.render(rawContent);
        Message message = new Message(chat, rawContent, rendered, Instant.now(clock));
        message = messageRepository.save(message);
        chatService.touchActivity(chat);

        MessageDto dto = MessageDto.from(message);
        messagingTemplate.convertAndSend("/topic/chat/" + chat.getCode(), dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> listMessages(String code, Pageable pageable) {
        Chat chat = chatService.getActiveChat(code);
        return messageRepository.findByChatOrderByCreatedAtAsc(chat, pageable).map(MessageDto::from);
    }
}
