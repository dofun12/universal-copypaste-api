package org.lemanoman.copypaste.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemanoman.copypaste.chat.Chat;
import org.lemanoman.copypaste.chat.ChatService;
import org.lemanoman.copypaste.common.ContentFormatter;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ContentFormatter contentFormatter;
    private Clock fixedClock;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        contentFormatter = new ContentFormatter();
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        messageService = new MessageService(messageRepository, chatService, contentFormatter, messagingTemplate, fixedClock);
    }

    @Test
    void postMessageSavesAndBroadcasts() {
        Chat chat = new Chat("AYBDC", Instant.now(fixedClock));
        when(chatService.getActiveChat("AYBDC")).thenReturn(chat);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageDto dto = messageService.postMessage("AYBDC", "hello https://example.com/pic.png");

        assertThat(dto.content()).isEqualTo("hello https://example.com/pic.png");
        assertThat(dto.renderedContent()).contains("<img");
        verify(chatService).touchActivity(chat);

        ArgumentCaptor<String> destination = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(destination.capture(), eq(dto));
        assertThat(destination.getValue()).isEqualTo("/topic/chat/AYBDC");
    }

    @Test
    void listMessagesDelegatesToActiveChat() {
        Chat chat = new Chat("AYBDC", Instant.now(fixedClock));
        when(chatService.getActiveChat("AYBDC")).thenReturn(chat);
        when(messageRepository.findByChatOrderByCreatedAtAsc(eq(chat), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        messageService.listMessages("AYBDC", org.springframework.data.domain.PageRequest.of(0, 10));

        verify(messageRepository).findByChatOrderByCreatedAtAsc(eq(chat), any());
    }
}
