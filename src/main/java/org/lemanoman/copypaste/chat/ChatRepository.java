package org.lemanoman.copypaste.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByCode(String code);

    boolean existsByCode(String code);

    List<Chat> findByLastActivityAtBefore(Instant threshold);
}
