package com.example.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.example.ai_service.model.EvaluateRecipeRequest;
import com.example.ai_service.model.GPTResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class AiService {

    private Object or;
    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    public GPTResponse evaluateRecipe(EvaluateRecipeRequest request) throws IOException {
        String prompt = buildPrompt(request);
        String gptJson = callOpenRouter(prompt);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(gptJson, GPTResponse.class);
    }

    private String buildPrompt(EvaluateRecipeRequest request) {
        return """
                Aşağıdaki başlık ve malzemelerle yemek yapılabilir mi?
                Başlık: %s
                Malzemeler: %s

                Lütfen sadece şu JSON formatında cevap ver:
                {
                  "exists": boolean,
                  "missingIngredients": [string],
                  "aiComment": string,
                  "suggestedRecipe": {
                    "title": string,
                    "steps": [string]
                  }
                }
                """.formatted(request.getTitle(), String.join(", ", request.getIngredients()));
    }

    private String callOpenRouter(String prompt) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("HTTP-Referer", "http://localhost"); // Gerekli olabilir

        String body = """
                {
                  "model": "%s",
                  "messages": [
                    { "role": "user", "content": "%s" }
                  ]
                }
                """.formatted(model, prompt.replace("\"", "\\\""));

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder responseText = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseText.append(line);
            }

            return extractJsonFromOpenRouterResponse(responseText.toString());
        }
    }

    private String extractJsonFromOpenRouterResponse(String response) {
        int index = response.indexOf("content\":\"");
        if (index == -1) return "{}";

        String json = response.substring(index + 10);
        json = json.replaceAll("\\\\n", "");
        json = json.replaceAll("\\\\\"", "\"");
        json = json.replaceAll("\"\\}\\}\\]$", "\"}");

        return json;
    }
}
