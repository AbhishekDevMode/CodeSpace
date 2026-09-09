package com.email.backend.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class AIRouter {

    private static final Logger log = LoggerFactory.getLogger(AIRouter.class);
    private final List<AIService> aiServices;

    @Autowired
    public AIRouter(List<AIService> aiServices) {
        this.aiServices = aiServices;
    }

    public String generateDocumentation(String codeContext, String prompt) {
        return executeWithFallback(
                service -> service.generateDocumentation(codeContext, prompt),
                "generateDocumentation"
        );
    }

    public String explainCode(String codeSnippet) {
        return executeWithFallback(
                service -> service.explainCode(codeSnippet),
                "explainCode"
        );
    }

    public String suggestImprovements(String codeSnippet) {
        return executeWithFallback(
                service -> service.suggestImprovements(codeSnippet),
                "suggestImprovements"
        );
    }

    private String executeWithFallback(Function<AIService, String> action, String operationName) {
        if (aiServices == null || aiServices.isEmpty()) {
            throw new IllegalStateException("No AI Service providers are currently configured or available.");
        }

        Exception lastException = null;
        for (AIService provider : aiServices) {
            try {
                log.info("Attempting AI operation '{}' with provider '{}'", operationName, provider.getProviderName());
                String result = action.apply(provider);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("AI Provider '{}' failed for operation '{}': {}", 
                        provider.getProviderName(), operationName, e.getMessage());
            }
        }

        String detail = (lastException != null && lastException.getMessage() != null) ? ": " + lastException.getMessage() : "";
        throw new RuntimeException("All AI Service providers failed for operation '" + operationName + "'" + detail, lastException);
    }

    public List<AIService> getAiServices() {
        return aiServices;
    }
}
