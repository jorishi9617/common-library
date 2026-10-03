package com.videoplatform.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.api-key=test-api-key-for-token-service",
        "security.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@AutoConfigureMockMvc
class TokenControllerTest {
    private static final String API_KEY = "test-api-key-for-token-service";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthIsPublicAndTokenRoutesRequireApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tokens"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void issuesAndValidatesTokens() throws Exception {
        String userId = UUID.randomUUID().toString();
        MvcResult issueResult = mockMvc.perform(post("/api/tokens")
                        .header("X-API-Key", API_KEY)
                        .contentType("application/json")
                        .content("""
                                {"userId":"%s","email":"user@example.com"}
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andReturn();

        String token = objectMapper.readTree(issueResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        mockMvc.perform(post("/api/tokens/validate")
                        .header("X-API-Key", API_KEY)
                        .contentType("application/json")
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
}
