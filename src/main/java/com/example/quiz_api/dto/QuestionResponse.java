package com.example.quiz_api.dto;

import com.example.quiz_api.model.Option;
import com.example.quiz_api.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for returning questions to quiz takers
 * Does NOT include correct answers
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {

    /**
     * Unique identifier for the question
     */
    private Long id;

    /**
     * The question text
     */
    private String text;

    /**
     * Type of question
     */
    private QuestionType type;

    /**
     * List of available options (for choice questions)
     */
    private List<Option> options;

    /**
     * Word limit for text questions
     */
    private Integer wordLimit;
}
