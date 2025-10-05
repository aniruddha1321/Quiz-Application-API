package com.example.quiz_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single option in a multiple choice or single choice question
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Option {

    /**
     * Unique identifier for the option
     */
    private Long id;

    /**
     * Text content of the option
     */
    private String text;
}
