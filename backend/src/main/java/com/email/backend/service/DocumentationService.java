package com.email.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentationService {

    private final ChatClient chatClient;

    public DocumentationService(@Autowired(required = false) ChatClient.Builder chatClientBuilder) {
        this.chatClient = (chatClientBuilder != null) ? chatClientBuilder.build() : null;
    }

    public String generateDocumentation(String codeContext, String className) {
        if (chatClient == null) {
            return "/**\n * Auto-generated JavaDoc placeholder for " + className + "\n */";
        }

        String prompt = """
            You are a senior software engineer. Generate JavaDoc for this class:
            %s
            
            Include:
            1. Class description
            2. Method descriptions with @param and @return tags
            3. Usage examples
            """.formatted(codeContext);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}