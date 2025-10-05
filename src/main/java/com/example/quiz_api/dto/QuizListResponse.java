package com.example.quiz_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for listing quizzes
 * Contains summary information about a quiz
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizListResponse {

    /**
     * Unique identifier for the quiz
     */
    private Long id;

    /**
     * Title of the quiz
     */
    private String title;

    /**
     * Number of questions in the quiz
     */
    private Integer questionCount;

    /**
     * When the quiz was created
     */
    private LocalDateTime createdAt;
}