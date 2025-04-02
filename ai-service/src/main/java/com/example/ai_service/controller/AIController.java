package com.example.ai_service.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.ai_service.model.EvaluateRecipeRequest;
import com.example.ai_service.model.GPTResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ai_service.service.AiService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor


public class AIController {

    private final AiService aiService;

    @PostMapping("/evaluate-recipe")
    public ResponseEntity<?> evaluateRecipe(@Valid  @RequestBody EvaluateRecipeRequest request) {
        try {
            GPTResponse response = aiService.evaluateRecipe(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "OpenRouter API iletişim hatası", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "İstek işlenemedi", "details", e.getMessage()));
        }
    }
}
