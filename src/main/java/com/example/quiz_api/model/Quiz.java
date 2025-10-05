package com.example.quiz_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a quiz containing multiple questions
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {

    /**
     * Unique identifier for the quiz
     */
    private Long id;

    /**
     * Title of the quiz
     */
    private String title;

    /**
     * List of question IDs belonging to this quiz
     */
    private List<Long> questionIds;

    /**
     * Timestamp when the quiz was created
     */
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a new quiz
     */
    public Quiz(Long id, String title) {
        this.id = id;
        this.title = title;
        this.questionIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
}
