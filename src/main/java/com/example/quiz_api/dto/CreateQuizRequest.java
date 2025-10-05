package com.example.quiz_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for creating a new quiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    /**
     * Title of the quiz to be created
     * Must not be blank
     */
    @NotBlank(message = "Quiz title is required")
    private String title;
}