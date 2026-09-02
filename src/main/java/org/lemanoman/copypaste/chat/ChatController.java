package org.lemanoman.copypaste.chat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatDto> createChat() {
        Chat chat = chatService.createChat();
        return ResponseEntity.status(HttpStatus.CREATED).body(ChatDto.from(chat));
    }

    @GetMapping("/{code}")
    public ChatDto getChat(@PathVariable String code) {
        return ChatDto.from(chatService.getActiveChat(code));
    }
}
