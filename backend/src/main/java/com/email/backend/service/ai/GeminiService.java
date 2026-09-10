package com.email.backend.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service("geminiService")
@ConditionalOnProperty(name = "ai.provider.gemini.enabled", havingValue = "true", matchIfMissing = true)
public class GeminiService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3.5-flash",
            "gemini-3.5-flash-lite",
            "gemini-3.6-flash",
            "gemini-3.8-flash"
    );

    private final String apiKey;
    private final String preferredModel;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Value("${gemini.api.key:${spring.ai.vertex.ai.gemini.api-key:}}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash}") String preferredModel
    ) {
        this.apiKey = sanitizeApiKey(apiKey);
        this.preferredModel = preferredModel != null ? preferredModel.trim() : "gemini-3.5-flash";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private static String sanitizeApiKey(String key) {
        if (key == null) return "";
        String trimmed = key.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            trimmed = trimmed.substring(7).trim();
        }
        return trimmed;
    }

    public String getMaskedApiKey() {

        if (apiKey.isEmpty()) {
            return "NOT_CONFIGURED";
        }
        if (apiKey.length() <= 8) {
            return "*** (length: " + apiKey.length() + ")";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4) + " (length: " + apiKey.length() + ")";
    }

    @Override
    public String getProviderName() {
        return "Gemini";
    }

    @Override
    public String generateDocumentation(String codeContext, String prompt) {
        log.info("GeminiService: Generating documentation...");
        String fullPrompt = (prompt != null && !prompt.isBlank())
                ? prompt + "\n\nCode Context:\n" + codeContext
                : "Generate comprehensive documentation (with descriptions, parameters, return values) for the following code:\n" + codeContext;
        return callGeminiWithFallback(fullPrompt);
    }

    @Override
    public String explainCode(String codeSnippet) {
        log.info("GeminiService: Explaining code...");
        String prompt = "Explain clearly what the following code does, its key logic, and purpose:\n" + codeSnippet;
        return callGeminiWithFallback(prompt);
    }

    @Override
    public String suggestImprovements(String codeSnippet) {
        log.info("GeminiService: Suggesting code improvements...");
        String prompt = "Suggest optimizations, design improvements, and best practices for this code:\n" + codeSnippet;
        return callGeminiWithFallback(prompt);
    }

    public String testConnection() {
        log.info("GeminiService: Testing API connection...");
        return callGeminiWithFallback("Respond with 'Gemini API is connected successfully!' and nothing else.");
    }

    private String callGeminiWithFallback(String promptText) {
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Please set gemini.api.key or GEMINI_API_KEY environment variable.");
        }

        Exception lastException = null;
        List<String> errors = new java.util.ArrayList<>();

        // Try preferred model first, then fallback models
        List<String> modelsToTry = preferredModel != null && !FALLBACK_MODELS.contains(preferredModel)
                ? java.util.stream.Stream.concat(java.util.stream.Stream.of(preferredModel), FALLBACK_MODELS.stream()).toList()
                : FALLBACK_MODELS;

        for (String model : modelsToTry) {
            try {
                return callGeminiModel(model, promptText);
            } catch (Exception e) {
                lastException = e;
                errors.add("[" + model + "] " + e.getMessage());
                log.error("Gemini model '{}' request failed: {}", model, e.getMessage());
            }
        }

        throw new RuntimeException("All Gemini model attempts failed. Details: " + String.join("; ", errors), lastException);
    }

    private String callGeminiModel(String model, String promptText) throws Exception {
        String endpoint = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptText)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 2048
                )
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            throw new RuntimeException("Unexpected response structure from Gemini API: " + response.body());
        } else {
            String errorMsg = response.body();
            try {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("error") && root.get("error").has("message")) {
                    errorMsg = root.get("error").get("message").asText();
                }
            } catch (Exception ignored) {}
            throw new RuntimeException("HTTP " + response.statusCode() + " from Gemini (" + model + "): " + errorMsg);
        }
    }
}
