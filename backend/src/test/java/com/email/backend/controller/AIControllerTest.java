package com.email.backend.controller;

import com.email.backend.security.CustomUserDetailsService;
import com.email.backend.security.JwtAuthenticationFilter;
import com.email.backend.security.JwtTokenProvider;
import com.email.backend.service.ai.AIRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIRouter aiRouter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGenerateDoc() throws Exception {
        Map<String, String> payload = Map.of("codeContext", "class Foo {}", "prompt", "Generate JavaDoc");
        when(aiRouter.generateDocumentation(anyString(), anyString())).thenReturn("Generated Javadoc");

        mockMvc.perform(post("/api/ai/doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentation").value("Generated Javadoc"));
    }

    @Test
    void testExplainCode() throws Exception {
        Map<String, String> payload = Map.of("codeSnippet", "public void foo() {}");
        when(aiRouter.explainCode(anyString())).thenReturn("Code explanation");

        mockMvc.perform(post("/api/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation").value("Code explanation"));
    }

    @Test
    void testSuggestImprovements() throws Exception {
        Map<String, String> payload = Map.of("codeSnippet", "public void foo() {}");
        when(aiRouter.suggestImprovements(anyString())).thenReturn("Improvement suggestions");

        mockMvc.perform(post("/api/ai/improve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").value("Improvement suggestions"));
    }

    @Test
    void testChatEndpoint() throws Exception {
        Map<String, String> payload = Map.of("message", "Explain what this class does", "codeContext", "class UserService {}");
        when(aiRouter.explainCode(anyString())).thenReturn("Explanation via chat");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Explanation via chat"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        when(aiRouter.explainCode(anyString())).thenReturn("Sample explanation");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("AI service is operational and responding."));
    }
}
