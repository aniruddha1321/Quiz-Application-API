package com.example.quiz_api.controller;

import com.example.quiz_api.dto.*;
import com.example.quiz_api.model.Question;
import com.example.quiz_api.model.Quiz;
import com.example.quiz_api.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Quiz API endpoints
 * Handles all HTTP requests related to quizzes
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    /**
     * Create a new quiz
     * POST /api/quizzes
     * @param request Quiz creation request
     * @return Created quiz
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Quiz>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {
        try {
            Quiz quiz = quizService.createQuiz(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(quiz));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all quizzes
     * GET /api/quizzes
     * @return List of all quizzes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizListResponse>>> getAllQuizzes() {
        try {
            List<QuizListResponse> quizzes = quizService.getAllQuizzes();
            return ResponseEntity.ok(ApiResponse.success(quizzes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add a question to a quiz
     * POST /api/quizzes/{quizId}/questions
     * @param quizId ID of the quiz
     * @param request Question details
     * @return Created question
     */
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<Question>> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody AddQuestionRequest request) {
        try {
            Question question = quizService.addQuestionToQuiz(quizId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(question));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all questions for a quiz
     * GET /api/quizzes/{quizId}/questions
     * @param quizId ID of the quiz
     * @return List of questions (without correct answers)
     */
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuizQuestions(
            @PathVariable Long quizId) {
        try {
            List<QuestionResponse> questions = quizService.getQuizQuestions(quizId);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Submit answers for a quiz
     * POST /api/quizzes/{quizId}/submit
     * @param quizId ID of the quiz
     * @param request Answer submission
     * @return Score and results
     */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<ApiResponse<SubmitAnswersResponse>> submitAnswers(
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitAnswersRequest request) {
        try {
            SubmitAnswersResponse response = quizService.submitQuizAnswers(quizId, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}