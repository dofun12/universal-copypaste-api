# AGENTS.md

Guidance for AI coding agents (and humans) working on this repository.

## Project purpose

**universal-copypaste-api** lets a user start a "chat" that gets a short, shareable code
(e.g. `AYBDC`). Anyone who enters that code — on any other device/browser — can view and post
to the same append-only message stream, in real time. It's a quick way to move text/links
between devices without accounts or file transfers.

## Tech stack

- Java 21, Spring Boot 4.1.x (Spring Framework 7 / Jakarta EE)
- Spring MVC (REST) + Spring Data JPA + H2 (file-based, persists to `./data/copypaste.mv.db`)
- Spring WebSocket with STOMP over SockJS for real-time message push
- Thymeleaf server-rendered pages + Bootstrap 5 (via CDN) for a mobile-first UI
- Bean Validation (Hibernate Validator) for request validation
- JUnit 5, Mockito, AssertJ, Spring Boot Test / MockMvc for testing

**Important — Jackson 3 / Spring Boot 4 gotchas:**
- This project targets Spring Boot 4, which ships Jackson 3 under the `tools.jackson.*`
  package (not `com.fasterxml.jackson.databind`). If you need `ObjectMapper` in code or tests,
  import `tools.jackson.databind.ObjectMapper`.
- `@AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure` and
  requires the `spring-boot-starter-webmvc-test` test dependency (already in `pom.xml`).
- Any Spring-managed bean class with **more than one constructor** must mark the one Spring
  should use with `@Autowired` (implicit single-constructor injection no longer applies once a
  second constructor exists, even if it's package-private and only used by tests).

## How to run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. Home page `/` lets you create or join a chat; a
chat room lives at `/chat/{code}`. The H2 database file lives at `./data/copypaste.mv.db`
(created on first run) so data survives restarts.

## How to test

```bash
mvn test
```

- Unit tests (`*ServiceTest`) mock repositories/collaborators and use a fixed `Clock` for
  deterministic time-based assertions (chat expiry, timestamps).
- Integration tests (`*ControllerTest`) boot the full Spring context with `@SpringBootTest` +
  `@AutoConfigureMockMvc`, using the `test` profile (`src/test/resources/application-test.yml`,
  H2 in-memory) so they don't touch the real `./data` file.

## Package layout (package-by-feature under `org.lemanoman.copypaste`)

```
config/    ChatProperties, WebSocketConfig — cross-cutting configuration
chat/      Chat entity/repository/service/controller, code cleanup job
message/   Message entity/repository/service/controller, DTOs
common/    CodeGenerator (chat codes), ContentFormatter (link/image rendering)
common/exception/  ChatNotFoundException + @RestControllerAdvice
web/       ViewController serving the Thymeleaf pages
```

Frontend assets live in `src/main/resources/{templates,static}`. `templates/layout.html`
holds shared Thymeleaf fragments (head/navbar/scripts) included by `index.html` and `chat.html`.
`static/js/app.js` handles the home page (create/join); `static/js/chat.js` handles the chat
room (initial REST fetch + STOMP/SockJS live updates + sending messages).

## API contract

- `POST /api/chats` → `201` `{ code, createdAt }` — creates a new chat with a generated code.
- `GET /api/chats/{code}` → `200` chat metadata, or `404` if the code is unknown/expired.
- `GET /api/chats/{code}/messages?page=&size=` → `200` a Spring `Page` of messages, oldest first.
- `POST /api/chats/{code}/messages` body `{ "content": "..." }` → `201` the created message
  (also broadcasts it over WebSocket); `400` if content is blank, `404` if the chat doesn't exist.
- WebSocket: connect to `/ws` (SockJS), subscribe to `/topic/chat/{code}` to receive new
  messages as they're posted. Message delivery is server-push only; clients still POST via REST
  to send (that's the single source of truth for persistence).

## Conventions

- Package-by-feature, not by layer: keep entity/repository/service/controller/DTO for a
  feature together in one package.
- No business logic in controllers — controllers translate HTTP <-> service calls only.
- Entities never leave the service layer as-is; controllers return DTOs (records).
- Chat codes are always normalized to uppercase (`ChatService.normalize`) before lookup.
- Message content is always HTML-escaped before any link/image markup is injected
  (`ContentFormatter`) — never trust raw user input in rendered HTML.
- Chat inactivity TTL, code length, and cleanup interval are configurable via
  `copypaste.chat.*` properties (see `application.yml` / `ChatProperties`) — don't hardcode them.
- Services that depend on wall-clock time take a `Clock` (defaulting to `Clock.systemUTC()`)
  so tests can inject a fixed clock instead of sleeping or mocking `Instant.now()` statically.

## Notes for future changes

- No authentication: the chat code itself is the shared secret. If auth is added later, it
  should sit in front of the existing REST/WebSocket layer without changing the core chat model.
- Cascade deletes on chat expiry are handled manually in `ChatCleanupJob` (delete messages, then
  the chat) rather than via JPA cascade, to keep `Chat` free of a `@OneToMany` collection.
