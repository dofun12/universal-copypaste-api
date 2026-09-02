package org.lemanoman.copypaste.message;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats/{code}/messages")
public class MessageController {

    private static final int DEFAULT_PAGE_SIZE = 200;

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Page<MessageDto> listMessages(@PathVariable String code,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageService.listMessages(code, pageable);
    }

    @PostMapping
    public ResponseEntity<MessageDto> postMessage(@PathVariable String code,
                                                   @Valid @RequestBody PostMessageRequest request) {
        MessageDto dto = messageService.postMessage(code, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
