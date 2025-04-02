package com.example.ai_service.model;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EvaluateRecipeRequest {

    @NotEmpty(message = "Tarif başlığı boş olamaz")
    private String title;

    @NotEmpty(message = "Malzeme Listesi boş olamaz")
    @Size(min = 1, message = "En az bir malzeme gerekli")
    private List<String> ingredients;

}
