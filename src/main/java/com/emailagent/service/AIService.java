package com.emailagent.service;

import com.emailagent.model.DecisionResult;
import com.emailagent.model.EmailMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public DecisionResult classify(EmailMessage email) {
        try {
            String prompt = "Classify this email. Respond with JSON only:\n"
                    + "{ \"intent\": \"...\", \"action\": \"reply|ignore|escalate|label\", \"confidence\": 0.0 }\n\n"
                    + "Rules: Use 'ignore' for social media, newsletters, notifications.\n"
                    + "Only use 'reply' for emails that genuinely need a human response.\n"
                    + "Never use 'notify' action.\n\n"
                    + "From: " + email.getFrom() + "\n"
                    + "Subject: " + email.getSubject() + "\n"
                    + "Body: " + email.getBody().substring(0, Math.min(email.getBody().length(), 300));

            String requestBody = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", model)
                            .put("max_tokens", 100)
                            .set("messages", objectMapper.createArrayNode()
                                    .add(objectMapper.createObjectNode()
                                            .put("role", "system")
                                            .put("content", "You are an email classifier. Always respond with valid JSON only. No extra text."))
                                    .add(objectMapper.createObjectNode()
                                            .put("role", "user")
                                            .put("content", prompt)))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return parseResponse(email.getMessageId(), response.body());

        } catch (Exception e) {
            System.err.println("AI error: " + e.getMessage());
            return fallbackDecision(email.getMessageId());
        }
    }

    private DecisionResult parseResponse(String emailId, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            content = content.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode result = objectMapper.readTree(content);
            String intent     = result.path("intent").asText("unknown");
            String action     = result.path("action").asText("ignore");
            double confidence = result.path("confidence").asDouble(0.5);

            System.out.println("AI → intent: " + intent
                    + " | action: " + action
                    + " | confidence: " + confidence);

            return new DecisionResult(emailId, "ai",
                    "AI Classification", action, null, "AI");

        } catch (Exception e) {
            System.err.println("Failed to parse AI response: " + e.getMessage());
            return fallbackDecision(emailId);
        }
    }

    private DecisionResult fallbackDecision(String emailId) {
        return new DecisionResult(emailId, "fallback",
                "Fallback", "ignore", null, "AI");
    }
}