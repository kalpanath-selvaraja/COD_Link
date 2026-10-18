package com.ijse.codlink.service.impl;

import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${spring.ai.google.genai.chat.model}")
    private String model;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final RestClient restClient;

    public GeminiServiceImpl() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(5));
        factory.setReadTimeout(java.time.Duration.ofSeconds(20));
        restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String generateReply(String prompt) {
        log.info("Execute method generateReply()");
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(BASE_URL + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new CustomException(502, "AI service unavailable");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            Object text = parts.get(0).get("text");
            if (!(text instanceof String reply) || reply.isBlank()) throw new CustomException(502, "AI service returned no answer");
            return reply;
        } catch (Exception e) {
            log.warn("AI service returned an invalid response");
            throw new CustomException(502, "AI service unavailable");
        }
    }
}