package com.example.ai_service.controller;


import lombok.RequiredArgsConstructor;
import com.example.ai_service.model.EvaluateRecipeRequest;
import com.example.ai_service.model.GPTResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ai_service.service.AiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AIController {

    private final AiService aiService;

    @PostMapping("/evaluate-recipe")
    public ResponseEntity<GPTResponse> evaluateRecipe(@RequestBody EvaluateRecipeRequest request) {
        try {
            GPTResponse response = aiService.evaluateRecipe(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(null); // İstersen hata mesajı döndüren bir error response modeli de tanımlayabiliriz
        }
    }
}
