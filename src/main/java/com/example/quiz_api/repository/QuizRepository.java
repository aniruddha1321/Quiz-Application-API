package com.example.quiz_api.repository;

import com.example.quiz_api.model.Question;
import com.example.quiz_api.model.Quiz;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository for managing quiz and question data
 * Uses in-memory storage with thread-safe collections
 */
@Repository
public class QuizRepository {

    // Thread-safe maps for storing data
    private final Map<Long, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Map<Long, Question> questions = new ConcurrentHashMap<>();

    // Thread-safe counters for generating unique IDs
    private final AtomicLong quizIdCounter = new AtomicLong(1);
    private final AtomicLong questionIdCounter = new AtomicLong(1);
    private final AtomicLong optionIdCounter = new AtomicLong(1);

    /**
     * Save or update a quiz
     * @param quiz The quiz to save
     * @return The saved quiz with generated ID if new
     */
    public Quiz saveQuiz(Quiz quiz) {
        if (quiz.getId() == null) {
            quiz.setId(quizIdCounter.getAndIncrement());
        }
        quizzes.put(quiz.getId(), quiz);
        return quiz;
    }

    /**
     * Find a quiz by its ID
     * @param id The quiz ID
     * @return Optional containing the quiz if found
     */
    public Optional<Quiz> findQuizById(Long id) {
        return Optional.ofNullable(quizzes.get(id));
    }

    /**
     * Get all quizzes
     * @return List of all quizzes
     */
    public List<Quiz> findAllQuizzes() {
        return new ArrayList<>(quizzes.values());
    }

    /**
     * Save or update a question
     * @param question The question to save
     * @return The saved question with generated ID if new
     */
    public Question saveQuestion(Question question) {
        if (question.getId() == null) {
            question.setId(questionIdCounter.getAndIncrement());
        }
        questions.put(question.getId(), question);
        return question;
    }

    /**
     * Find a question by its ID
     * @param id The question ID
     * @return Optional containing the question if found
     */
    public Optional<Question> findQuestionById(Long id) {
        return Optional.ofNullable(questions.get(id));
    }

    /**
     * Generate a unique option ID
     * @return New unique option ID
     */
    public Long generateOptionId() {
        return optionIdCounter.getAndIncrement();
    }

    /**
     * Clear all data (useful for testing)
     */
    public void clear() {
        quizzes.clear();
        questions.clear();
        quizIdCounter.set(1);
        questionIdCounter.set(1);
        optionIdCounter.set(1);
    }
}