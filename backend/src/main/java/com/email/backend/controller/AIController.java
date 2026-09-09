package com.email.backend.controller;

import com.email.backend.service.ai.AIRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIRouter aiRouter;

    public AIController(AIRouter aiRouter) {
        this.aiRouter = aiRouter;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            String testReply = aiRouter.explainCode("public int add(int a, int b) { return a + b; }");
            health.put("status", "UP");
            health.put("message", "AI service is operational and responding.");
            health.put("sampleResponse", testReply);
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @PostMapping("/doc")
    public ResponseEntity<Map<String, String>> generateDoc(@RequestBody Map<String, String> payload) {
        String codeContext = payload.getOrDefault("codeContext", "");
        String prompt = payload.getOrDefault("prompt", "");
        String result = aiRouter.generateDocumentation(codeContext, prompt);
        
        Map<String, String> response = new HashMap<>();
        response.put("documentation", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, String>> explainCode(@RequestBody Map<String, String> payload) {
        String codeSnippet = payload.getOrDefault("codeSnippet", "");
        String result = aiRouter.explainCode(codeSnippet);

        Map<String, String> response = new HashMap<>();
        response.put("explanation", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/improve")
    public ResponseEntity<Map<String, String>> suggestImprovements(@RequestBody Map<String, String> payload) {
        String codeSnippet = payload.getOrDefault("codeSnippet", "");
        String result = aiRouter.suggestImprovements(codeSnippet);

        Map<String, String> response = new HashMap<>();
        response.put("suggestions", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "");
        String codeContext = payload.getOrDefault("codeContext", "");

        String promptContext = (codeContext != null && !codeContext.isBlank())
                ? "Code Context:\n" + codeContext + "\n\nUser Question/Request:\n" + message
                : message;

        String result;
        if (message.toLowerCase().contains("improve") || message.toLowerCase().contains("optimize")) {
            result = aiRouter.suggestImprovements(promptContext);
        } else if (message.toLowerCase().contains("explain") || message.toLowerCase().contains("what does")) {
            result = aiRouter.explainCode(promptContext);
        } else {
            result = aiRouter.generateDocumentation(codeContext.isEmpty() ? message : codeContext, message);
        }

        Map<String, String> response = new HashMap<>();
        response.put("reply", result);
        return ResponseEntity.ok(response);
    }
}
