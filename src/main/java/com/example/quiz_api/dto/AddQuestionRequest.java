package com.example.quiz_api.dto;

import com.example.quiz_api.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for adding a question to a quiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionRequest {

    /**
     * The question text
     */
    @NotBlank(message = "Question text is required")
    private String text;

    /**
     * Type of question (SINGLE, MULTIPLE, TEXT)
     */
    @NotNull(message = "Question type is required")
    private QuestionType type;

    /**
     * List of option texts for choice-based questions
     * Required for SINGLE and MULTIPLE types
     */
    private List<String> options;

    /**
     * List of correct answer indices for choice-based questions
     * Required for SINGLE and MULTIPLE types
     */
    private List<Integer> correctAnswers;

    /**
     * List of correct text answers for text-based questions
     * Required for TEXT type
     */
    private List<String> correctAnswerTexts;

    /**
     * Maximum character limit for text-based questions
     * Optional, defaults to 300
     */
    private Integer wordLimit;
}