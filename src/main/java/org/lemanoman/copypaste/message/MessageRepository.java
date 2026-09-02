package org.lemanoman.copypaste.message;

import org.lemanoman.copypaste.chat.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatOrderByCreatedAtAsc(Chat chat, Pageable pageable);

    long deleteByChat(Chat chat);
}
