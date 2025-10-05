package com.example.quiz_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for submitting quiz answers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswersRequest {

    /**
     * List of answers for each question
     */
    @NotNull(message = "Answers are required")
    private List<Answer> answers;

    /**
     * Represents an answer to a single question
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {

        /**
         * ID of the question being answered
         */
        @NotNull(message = "Question ID is required")
        private Long questionId;

        /**
         * List of selected option IDs
         * For choice questions: contains option IDs
         * For text questions: would need different handling in real implementation
         */
        @NotNull(message = "Selected options are required")
        private List<Long> selectedOptions;
    }
}