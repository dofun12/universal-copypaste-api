package org.lemanoman.copypaste.message;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createChat() throws Exception {
        String response = mockMvc.perform(post("/api/chats"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("code").asText();
    }

    @Test
    void postAndListMessages() throws Exception {
        String code = createChat();

        mockMvc.perform(post("/api/chats/" + code + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello world\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello world"))
                .andExpect(jsonPath("$.renderedContent").value("hello world"));

        mockMvc.perform(get("/api/chats/" + code + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].content").value("hello world"));
    }

    @Test
    void postBlankMessageReturns400() throws Exception {
        String code = createChat();

        mockMvc.perform(post("/api/chats/" + code + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postMessageToUnknownChatReturns404() throws Exception {
        mockMvc.perform(post("/api/chats/ZZZZZ/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void urlIsAutoLinkedInRenderedContent() throws Exception {
        String code = createChat();

        mockMvc.perform(post("/api/chats/" + code + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"check https://example.com/page out\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.renderedContent").value(
                        "check <a href=\"https://example.com/page\" target=\"_blank\" rel=\"noopener noreferrer\">https://example.com/page</a> out"));
    }
}
