package com.example.ai_service.model;


import lombok.Data;

import java.util.List;

@Data
public class EvaluateRecipeRequest {
    private String title;
    private List<String> ingredients;

}
