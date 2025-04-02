package com.example.ai_service.model;


import lombok.Data;

import java.util.List;

@Data
public class GPTResponse {

    private boolean exists;
    private List<String> missingIngredients;
    private String aiComment;
    private SuggestedRecipe suggestedRecipe;

    @Data
    public static class SuggestedRecipe{
        //2.dönen değelerder list<str> revizesi yapıldı.
        private String title;
        private List<String> steps;
    }

}
