package com.email.backend.service.ai;

public interface AIService {
    String getProviderName();
    String generateDocumentation(String codeContext, String prompt);
    String explainCode(String codeSnippet);
    String suggestImprovements(String codeSnippet);
}
