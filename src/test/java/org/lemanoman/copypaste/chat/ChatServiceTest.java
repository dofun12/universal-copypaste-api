package org.lemanoman.copypaste.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemanoman.copypaste.common.CodeGenerator;
import org.lemanoman.copypaste.common.exception.ChatNotFoundException;
import org.lemanoman.copypaste.config.ChatProperties;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private CodeGenerator codeGenerator;

    private ChatProperties chatProperties;
    private Clock fixedClock;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatProperties = new ChatProperties();
        chatProperties.setTtl(Duration.ofHours(24));
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        chatService = new ChatService(chatRepository, codeGenerator, chatProperties, fixedClock);
    }

    @Test
    void createChatGeneratesCodeAndPersists() {
        when(codeGenerator.generateUniqueCode()).thenReturn("AYBDC");
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Chat chat = chatService.createChat();

        assertThat(chat.getCode()).isEqualTo("AYBDC");
        assertThat(chat.getCreatedAt()).isEqualTo(Instant.now(fixedClock));
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void getActiveChatReturnsChatWhenNotExpired() {
        Chat chat = new Chat("AYBDC", Instant.now(fixedClock));
        when(chatRepository.findByCode("AYBDC")).thenReturn(Optional.of(chat));

        Chat result = chatService.getActiveChat("aybdc");

        assertThat(result).isSameAs(chat);
    }

    @Test
    void getActiveChatThrowsWhenCodeUnknown() {
        when(chatRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getActiveChat("missing"))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void getActiveChatThrowsWhenExpired() {
        Instant longAgo = Instant.now(fixedClock).minus(Duration.ofHours(25));
        Chat chat = new Chat("AYBDC", longAgo);
        when(chatRepository.findByCode("AYBDC")).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.getActiveChat("AYBDC"))
                .isInstanceOf(ChatNotFoundException.class);
    }
}
