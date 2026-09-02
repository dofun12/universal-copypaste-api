package org.lemanoman.copypaste.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createChatReturnsGeneratedCode() throws Exception {
        mockMvc.perform(post("/api/chats"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(matchesPattern("[A-Z]{5}")))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getUnknownChatReturns404() throws Exception {
        mockMvc.perform(get("/api/chats/ZZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getChatAfterCreationSucceeds() throws Exception {
        String response = mockMvc.perform(post("/api/chats"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String code = objectMapper.readTree(response).get("code").asText();

        mockMvc.perform(get("/api/chats/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code));
    }
}
