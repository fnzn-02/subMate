package com.onAir.submate.domain.chat.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            RestClient geminiRestClient,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {
        this.restClient = geminiRestClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generate(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );

        GeminiApiResponse response = restClient.post()
                .uri("/{model}:generateContent?key={key}", model, apiKey)
                .body(requestBody)
                .retrieve()
                .body(GeminiApiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            log.warn("Gemini API 응답이 비어있습니다.");
            return "죄송합니다. 응답을 생성하지 못했습니다.";
        }

        return response.candidates().get(0)
                .content()
                .parts().get(0)
                .text();
    }

    public String generateWithImage(String prompt, byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("inlineData", Map.of(
                                        "mimeType", mimeType,
                                        "data", base64Image
                                )),
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 2048
                )
        );

        GeminiApiResponse response = restClient.post()
                .uri("/{model}:generateContent?key={key}", model, apiKey)
                .body(requestBody)
                .retrieve()
                .body(GeminiApiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            log.warn("Gemini Vision API 응답이 비어있습니다.");
            return "";
        }

        return response.candidates().get(0)
                .content()
                .parts().get(0)
                .text();
    }

    // Gemini REST API 응답 구조
    record GeminiApiResponse(List<Candidate> candidates) {}
    record Candidate(Content content) {}
    record Content(List<Part> parts) {}
    record Part(String text) {}
}
