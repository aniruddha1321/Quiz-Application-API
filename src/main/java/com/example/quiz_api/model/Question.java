package com.example.quiz_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a question in a quiz
 * Supports single choice, multiple choice, and text-based questions
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {

    /**
     * Unique identifier for the question
     */
    private Long id;

    /**
     * ID of the quiz this question belongs to
     */
    private Long quizId;

    /**
     * The question text
     */
    private String text;

    /**
     * Type of question (SINGLE, MULTIPLE, TEXT)
     */
    private QuestionType type;

    /**
     * List of options for choice-based questions
     * Null for text questions
     */
    private List<Option> options;

    /**
     * List of correct option IDs for choice-based questions
     * Null for text questions
     */
    private List<Long> correctAnswerIds;

    /**
     * List of correct text answers for text-based questions
     * Null for choice-based questions
     */
    private List<String> correctAnswerTexts;

    /**
     * Maximum character limit for text-based questions
     * Null for choice-based questions
     */
    private Integer wordLimit;
}
