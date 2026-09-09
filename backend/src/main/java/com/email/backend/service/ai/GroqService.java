package com.email.backend.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("groqService")
@ConditionalOnProperty(name = "ai.provider.groq.enabled", havingValue = "true", matchIfMissing = true)
public class GroqService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);
    private final ChatClient chatClient;

    public GroqService(@Autowired(required = false) ChatClient.Builder chatClientBuilder) {
        this.chatClient = (chatClientBuilder != null) ? chatClientBuilder.build() : null;
    }

    @Override
    public String getProviderName() {
        return "Groq";
    }

    @Override
    public String generateDocumentation(String codeContext, String prompt) {
        log.info("GroqService: Generating documentation...");
        if (chatClient == null) {
            throw new IllegalStateException("Groq ChatClient is not configured.");
        }
        String fullPrompt = (prompt != null && !prompt.isBlank())
                ? prompt + "\n\nCode Context:\n" + codeContext
                : "Generate comprehensive documentation for the following code context:\n" + codeContext;
        return chatClient.prompt().user(fullPrompt).call().content();
    }

    @Override
    public String explainCode(String codeSnippet) {
        log.info("GroqService: Explaining code...");
        if (chatClient == null) {
            throw new IllegalStateException("Groq ChatClient is not configured.");
        }
        String prompt = "Explain clearly what the following code does:\n" + codeSnippet;
        return chatClient.prompt().user(prompt).call().content();
    }

    @Override
    public String suggestImprovements(String codeSnippet) {
        log.info("GroqService: Suggesting code improvements...");
        if (chatClient == null) {
            throw new IllegalStateException("Groq ChatClient is not configured.");
        }
        String prompt = "Suggest code improvements and optimizations for the following code:\n" + codeSnippet;
        return chatClient.prompt().user(prompt).call().content();
    }
}
