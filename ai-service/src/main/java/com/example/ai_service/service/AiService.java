package com.example.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import com.example.ai_service.model.EvaluateRecipeRequest;
import com.example.ai_service.model.GPTResponse;
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

    ///service information
    private final Dotenv dotenv = Dotenv.load();

    private final String apiKey = dotenv.get("OPENROUTER_API_KEY");
    private final String apiUrl = dotenv.get("OPENROUTER_API_URL");
    private final String model  = dotenv.get("OPENROUTER_MODEL");



    public GPTResponse evaluateRecipe(EvaluateRecipeRequest request) throws IOException {
        String prompt = buildPrompt(request);
        String gptJson = callOpenRouter(prompt);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(gptJson, GPTResponse.class);
    }


    /// promp düzeltildi


    private String buildPrompt(EvaluateRecipeRequest request) {
        return """
        Aşağıdaki başlık ve malzemelerle yemek yapılabilir mi?
        
        ● Eğer başlık saçma veya tanınmayan bir yemekse (örneğin: "1234", "xzy", "asdf"), sadece şu cevabı dön:
        {
          "exists": false,
          "missingIngredients": [],
          "aiComment": "Bu başlık bilinen bir yemek değil.",
          "suggestedRecipe": null
        }

        ● Eğer başlık tanıdık bir yemekse ama malzemeler eksikse:
        - Eksik malzemeleri "missingIngredients" içinde belirt.
        - Kısa bir açıklama "aiComment" içine yaz.
        - Uygun şekilde bir "suggestedRecipe" döndür (basit adımlarla).
        
        ● Eğer malzemeler yeterliyse:
        - "exists": true,
        - "missingIngredients": boş liste olmalı.
        - "aiComment" ve "suggestedRecipe" dolu olmalı.

        Lütfen sadece aşağıdaki JSON formatında ve geçerli içeriklerle dön:
        {
          "exists": boolean,
          "missingIngredients": [string],
          "aiComment": string,
          "suggestedRecipe": {
            "title": string,
            "steps": [string]
          }
        }

        Başlık: %s
        Malzemeler: %s
        """.formatted(request.getTitle(), String.join(", ", request.getIngredients()));
    }



    private String callOpenRouter(String prompt) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("HTTP-Referer", "http://localhost");

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

    ///response edilen yanıtın temizliği (anlamlandırmak için)
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
