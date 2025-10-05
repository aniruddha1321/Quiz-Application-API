package com.example.quiz_api.service;

import com.example.quiz_api.dto.*;
import com.example.quiz_api.model.*;
import com.example.quiz_api.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer containing business logic for quiz operations
 */
@Service
public class QuizService {

    @Autowired
    private QuizRepository repository;

    /**
     * Create a new quiz
     * @param request Quiz creation request with title
     * @return Created quiz
     */
    public Quiz createQuiz(CreateQuizRequest request) {
        String title = request.getTitle().trim();
        Quiz quiz = new Quiz(null, title);
        return repository.saveQuiz(quiz);
    }

    /**
     * Get all quizzes with metadata
     * @return List of quiz summaries
     */
    public List<QuizListResponse> getAllQuizzes() {
        return repository.findAllQuizzes().stream()
                .map(quiz -> new QuizListResponse(
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getQuestionIds().size(),
                        quiz.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get a quiz by ID
     * @param quizId The quiz ID
     * @return The quiz
     * @throws IllegalArgumentException if quiz not found
     */
    public Quiz getQuizById(Long quizId) {
        return repository.findQuizById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
    }

    /**
     * Add a question to a quiz
     * @param quizId ID of the quiz
     * @param request Question details
     * @return Created question
     */
    public Question addQuestionToQuiz(Long quizId, AddQuestionRequest request) {
        Quiz quiz = getQuizById(quizId);

        // Validate question data
        validateQuestion(request);

        Question question = new Question();
        question.setQuizId(quizId);
        question.setText(request.getText());
        question.setType(request.getType());

        // Handle choice-based questions
        if (request.getType() == QuestionType.SINGLE ||
                request.getType() == QuestionType.MULTIPLE) {

            // Create options with unique IDs
            List<Option> options = request.getOptions().stream()
                    .map(optText -> new Option(repository.generateOptionId(), optText))
                    .collect(Collectors.toList());
            question.setOptions(options);

            // Map correct answer indices to option IDs
            List<Long> correctIds = request.getCorrectAnswers().stream()
                    .map(idx -> options.get(idx).getId())
                    .collect(Collectors.toList());
            question.setCorrectAnswerIds(correctIds);
        }
        // Handle text-based questions
        else if (request.getType() == QuestionType.TEXT) {
            question.setCorrectAnswerTexts(request.getCorrectAnswerTexts());
            question.setWordLimit(request.getWordLimit() != null ?
                    request.getWordLimit() : 300);
        }

        // Save question and add to quiz
        Question savedQuestion = repository.saveQuestion(question);
        quiz.getQuestionIds().add(savedQuestion.getId());
        repository.saveQuiz(quiz);

        return savedQuestion;
    }

    /**
     * Validate question data based on type
     * @param request Question request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateQuestion(AddQuestionRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Question text is required");
        }

        QuestionType type = request.getType();

        // Validate single choice questions
        if (type == QuestionType.SINGLE) {
            if (request.getOptions() == null || request.getOptions().size() < 2) {
                throw new IllegalArgumentException(
                        "Single choice questions must have at least 2 options");
            }
            if (request.getCorrectAnswers() == null ||
                    request.getCorrectAnswers().size() != 1) {
                throw new IllegalArgumentException(
                        "Single choice questions must have exactly 1 correct answer");
            }
            int correctIdx = request.getCorrectAnswers().get(0);
            if (correctIdx < 0 || correctIdx >= request.getOptions().size()) {
                throw new IllegalArgumentException("Invalid correct answer index");
            }
        }

        // Validate multiple choice questions
        if (type == QuestionType.MULTIPLE) {
            if (request.getOptions() == null || request.getOptions().size() < 2) {
                throw new IllegalArgumentException(
                        "Multiple choice questions must have at least 2 options");
            }
            if (request.getCorrectAnswers() == null ||
                    request.getCorrectAnswers().isEmpty()) {
                throw new IllegalArgumentException(
                        "Multiple choice questions must have at least 1 correct answer");
            }
            for (int idx : request.getCorrectAnswers()) {
                if (idx < 0 || idx >= request.getOptions().size()) {
                    throw new IllegalArgumentException("Invalid correct answer index");
                }
            }
        }

        // Validate text questions
        if (type == QuestionType.TEXT) {
            if (request.getCorrectAnswerTexts() == null ||
                    request.getCorrectAnswerTexts().isEmpty()) {
                throw new IllegalArgumentException(
                        "Text questions must have at least 1 correct answer");
            }
            Integer limit = request.getWordLimit() != null ?
                    request.getWordLimit() : 300;
            if (limit < 1 || limit > 300) {
                throw new IllegalArgumentException(
                        "Word limit must be between 1 and 300 characters");
            }
        }
    }

    /**
     * Get all questions for a quiz (without correct answers)
     * @param quizId ID of the quiz
     * @return List of questions for quiz takers
     */
    public List<QuestionResponse> getQuizQuestions(Long quizId) {
        Quiz quiz = getQuizById(quizId);

        return quiz.getQuestionIds().stream()
                .map(questionId -> repository.findQuestionById(questionId)
                        .orElseThrow(() -> new IllegalArgumentException("Question not found")))
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getText(),
                        question.getType(),
                        question.getOptions(),
                        question.getWordLimit()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Submit and score quiz answers
     * @param quizId ID of the quiz
     * @param request Answer submission request
     * @return Scoring results
     */
    public SubmitAnswersResponse submitQuizAnswers(Long quizId, SubmitAnswersRequest request) {
        Quiz quiz = getQuizById(quizId);

        int score = 0;
        List<SubmitAnswersResponse.QuestionResult> results = new ArrayList<>();

        for (SubmitAnswersRequest.Answer answer : request.getAnswers()) {
            Question question = repository.findQuestionById(answer.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid question ID: " + answer.getQuestionId()));

            if (!question.getQuizId().equals(quizId)) {
                throw new IllegalArgumentException(
                        "Question does not belong to this quiz");
            }

            boolean isCorrect = checkAnswer(question, answer.getSelectedOptions());
            if (isCorrect) {
                score++;
            }

            results.add(new SubmitAnswersResponse.QuestionResult(
                    answer.getQuestionId(), isCorrect));
        }

        return new SubmitAnswersResponse(score, quiz.getQuestionIds().size(), results);
    }

    /**
     * Check if an answer is correct
     * @param question The question
     * @param selectedOptions Selected option IDs or text
     * @return true if correct, false otherwise
     */
    private boolean checkAnswer(Question question, List<Long> selectedOptions) {
        // Check single choice
        if (question.getType() == QuestionType.SINGLE) {
            return selectedOptions.size() == 1 &&
                    question.getCorrectAnswerIds().contains(selectedOptions.get(0));
        }

        // Check multiple choice
        if (question.getType() == QuestionType.MULTIPLE) {
            if (selectedOptions.size() != question.getCorrectAnswerIds().size()) {
                return false;
            }
            List<Long> sortedSelected = new ArrayList<>(selectedOptions);
            List<Long> sortedCorrect = new ArrayList<>(question.getCorrectAnswerIds());
            Collections.sort(sortedSelected);
            Collections.sort(sortedCorrect);
            return sortedSelected.equals(sortedCorrect);
        }

        // Check text answer
        if (question.getType() == QuestionType.TEXT) {
            if (selectedOptions.isEmpty()) return false;

            // Note: In real implementation, text would be passed differently
            String answer = String.valueOf(selectedOptions.get(0)).trim().toLowerCase();

            int limit = question.getWordLimit() != null ?
                    question.getWordLimit() : 300;

            if (answer.length() > limit) {
                return false;
            }

            return question.getCorrectAnswerTexts().stream()
                    .anyMatch(correct -> correct.toLowerCase().equals(answer));
        }

        return false;
    }
}