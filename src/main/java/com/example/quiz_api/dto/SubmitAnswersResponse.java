package com.example.quiz_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for quiz submission results
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswersResponse {

    /**
     * Number of correct answers
     */
    private Integer score;

    /**
     * Total number of questions in the quiz
     */
    private Integer total;

    /**
     * Detailed results for each question
     */
    private List<QuestionResult> results;

    /**
     * Result for a single question
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResult {

        /**
         * ID of the question
         */
        private Long questionId;

        /**
         * Whether the answer was correct
         */
        private Boolean correct;
    }
}