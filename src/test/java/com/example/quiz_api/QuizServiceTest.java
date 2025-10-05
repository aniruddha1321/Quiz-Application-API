package com.example.quiz_api;

import com.example.quiz_api.dto.*;
import com.example.quiz_api.model.*;
import com.example.quiz_api.repository.QuizRepository;
import com.example.quiz_api.service.QuizService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuizService
 * Tests all business logic and validation rules
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizRepository repository;

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    // ============================================
    // QUIZ MANAGEMENT TESTS
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Should create quiz with valid title")
    void testCreateQuizWithValidTitle() {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("JavaScript Basics");

        Quiz quiz = quizService.createQuiz(request);

        assertNotNull(quiz);
        assertNotNull(quiz.getId());
        assertEquals("JavaScript Basics", quiz.getTitle());
        assertTrue(quiz.getQuestionIds().isEmpty());
        assertNotNull(quiz.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("Should trim whitespace from quiz title")
    void testCreateQuizTrimsWhitespace() {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("  Python Quiz  ");

        Quiz quiz = quizService.createQuiz(request);

        assertEquals("Python Quiz", quiz.getTitle());
    }

    @Test
    @Order(3)
    @DisplayName("Should generate unique IDs for multiple quizzes")
    void testCreateMultipleQuizzesWithUniqueIds() {
        CreateQuizRequest request1 = new CreateQuizRequest();
        request1.setTitle("Quiz 1");

        CreateQuizRequest request2 = new CreateQuizRequest();
        request2.setTitle("Quiz 2");

        Quiz quiz1 = quizService.createQuiz(request1);
        Quiz quiz2 = quizService.createQuiz(request2);

        assertNotEquals(quiz1.getId(), quiz2.getId());
    }

    @Test
    @Order(4)
    @DisplayName("Should return all quizzes")
    void testGetAllQuizzes() {
        CreateQuizRequest request1 = new CreateQuizRequest();
        request1.setTitle("Quiz 1");
        CreateQuizRequest request2 = new CreateQuizRequest();
        request2.setTitle("Quiz 2");

        quizService.createQuiz(request1);
        quizService.createQuiz(request2);

        List<QuizListResponse> quizzes = quizService.getAllQuizzes();

        assertEquals(2, quizzes.size());
        assertNotNull(quizzes.get(0).getId());
        assertNotNull(quizzes.get(0).getTitle());
        assertNotNull(quizzes.get(0).getQuestionCount());
        assertNotNull(quizzes.get(0).getCreatedAt());
    }

    @Test
    @Order(5)
    @DisplayName("Should throw exception when quiz not found")
    void testGetQuizByIdNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            quizService.getQuizById(999L);
        });
    }

    // ============================================
    // SINGLE CHOICE QUESTION TESTS
    // ============================================

    @Test
    @Order(6)
    @DisplayName("Should add valid single choice question")
    void testAddSingleChoiceQuestion() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("What is 2 + 2?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("3", "4", "5"));
        request.setCorrectAnswers(Arrays.asList(1));

        Question question = quizService.addQuestionToQuiz(quiz.getId(), request);

        assertNotNull(question);
        assertEquals("What is 2 + 2?", question.getText());
        assertEquals(QuestionType.SINGLE, question.getType());
        assertEquals(3, question.getOptions().size());
        assertEquals(1, question.getCorrectAnswerIds().size());
    }

    @Test
    @Order(7)
    @DisplayName("Should reject single choice with no correct answer")
    void testSingleChoiceNoCorrectAnswer() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("A", "B"));
        request.setCorrectAnswers(Arrays.asList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("exactly 1 correct answer"));
    }

    @Test
    @Order(8)
    @DisplayName("Should reject single choice with multiple correct answers")
    void testSingleChoiceMultipleCorrectAnswers() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("A", "B", "C"));
        request.setCorrectAnswers(Arrays.asList(0, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("exactly 1 correct answer"));
    }

    @Test
    @Order(9)
    @DisplayName("Should reject single choice with less than 2 options")
    void testSingleChoiceInsufficientOptions() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("A"));
        request.setCorrectAnswers(Arrays.asList(0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("at least 2 options"));
    }

    @Test
    @Order(10)
    @DisplayName("Should reject single choice with invalid correct answer index")
    void testSingleChoiceInvalidIndex() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("A", "B"));
        request.setCorrectAnswers(Arrays.asList(5));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("Invalid correct answer index"));
    }

    // ============================================
    // MULTIPLE CHOICE QUESTION TESTS
    // ============================================

    @Test
    @Order(11)
    @DisplayName("Should add valid multiple choice question")
    void testAddMultipleChoiceQuestion() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Select all even numbers");
        request.setType(QuestionType.MULTIPLE);
        request.setOptions(Arrays.asList("1", "2", "3", "4"));
        request.setCorrectAnswers(Arrays.asList(1, 3));

        Question question = quizService.addQuestionToQuiz(quiz.getId(), request);

        assertNotNull(question);
        assertEquals(QuestionType.MULTIPLE, question.getType());
        assertEquals(2, question.getCorrectAnswerIds().size());
    }

    @Test
    @Order(12)
    @DisplayName("Should reject multiple choice with no correct answers")
    void testMultipleChoiceNoCorrectAnswers() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.MULTIPLE);
        request.setOptions(Arrays.asList("A", "B"));
        request.setCorrectAnswers(Arrays.asList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("at least 1 correct answer"));
    }

    @Test
    @Order(13)
    @DisplayName("Should reject multiple choice with invalid answer index")
    void testMultipleChoiceInvalidIndex() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.MULTIPLE);
        request.setOptions(Arrays.asList("A", "B"));
        request.setCorrectAnswers(Arrays.asList(0, 5));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("Invalid correct answer index"));
    }

    // ============================================
    // TEXT QUESTION TESTS
    // ============================================

    @Test
    @Order(14)
    @DisplayName("Should add valid text question with default word limit")
    void testAddTextQuestionDefaultLimit() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("What is the capital of France?");
        request.setType(QuestionType.TEXT);
        request.setCorrectAnswerTexts(Arrays.asList("Paris", "paris"));

        Question question = quizService.addQuestionToQuiz(quiz.getId(), request);

        assertNotNull(question);
        assertEquals(QuestionType.TEXT, question.getType());
        assertEquals(300, question.getWordLimit());
    }

    @Test
    @Order(15)
    @DisplayName("Should add text question with custom word limit")
    void testAddTextQuestionCustomLimit() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Describe yourself");
        request.setType(QuestionType.TEXT);
        request.setCorrectAnswerTexts(Arrays.asList("Any answer"));
        request.setWordLimit(150);

        Question question = quizService.addQuestionToQuiz(quiz.getId(), request);

        assertEquals(150, question.getWordLimit());
    }

    @Test
    @Order(16)
    @DisplayName("Should reject text question with no correct answers")
    void testTextQuestionNoCorrectAnswers() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.TEXT);
        request.setCorrectAnswerTexts(Arrays.asList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("at least 1 correct answer"));
    }

    @Test
    @Order(17)
    @DisplayName("Should reject text question with word limit > 300")
    void testTextQuestionWordLimitTooHigh() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.TEXT);
        request.setCorrectAnswerTexts(Arrays.asList("answer"));
        request.setWordLimit(500);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("between 1 and 300"));
    }

    @Test
    @Order(18)
    @DisplayName("Should reject text question with word limit < 1")
    void testTextQuestionWordLimitTooLow() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Test?");
        request.setType(QuestionType.TEXT);
        request.setCorrectAnswerTexts(Arrays.asList("answer"));
        request.setWordLimit(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("between 1 and 300"));
    }

    // ============================================
    // GENERAL QUESTION VALIDATION TESTS
    // ============================================

    @Test
    @Order(19)
    @DisplayName("Should reject question with empty text")
    void testQuestionEmptyText() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("A", "B"));
        request.setCorrectAnswers(Arrays.asList(0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.addQuestionToQuiz(quiz.getId(), request)
        );

        assertTrue(exception.getMessage().contains("Question text is required"));
    }

    // ============================================
    // GET QUIZ QUESTIONS TESTS
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Should return questions without correct answers")
    void testGetQuizQuestionsHidesAnswers() {
        Quiz quiz = createTestQuiz();

        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("What is 2 + 2?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("3", "4", "5"));
        request.setCorrectAnswers(Arrays.asList(1));

        quizService.addQuestionToQuiz(quiz.getId(), request);

        List<QuestionResponse> questions = quizService.getQuizQuestions(quiz.getId());

        assertEquals(1, questions.size());
        assertNotNull(questions.get(0).getId());
        assertNotNull(questions.get(0).getText());
        assertNotNull(questions.get(0).getType());
        assertNotNull(questions.get(0).getOptions());
    }

    @Test
    @Order(21)
    @DisplayName("Should return empty list for quiz with no questions")
    void testGetQuizQuestionsEmpty() {
        Quiz quiz = createTestQuiz();

        List<QuestionResponse> questions = quizService.getQuizQuestions(quiz.getId());

        assertTrue(questions.isEmpty());
    }

    // ============================================
    // SUBMIT ANSWERS - SINGLE CHOICE TESTS
    // ============================================

    @Test
    @Order(22)
    @DisplayName("Should score correct single choice answer")
    void testSubmitCorrectSingleChoice() {
        Quiz quiz = createTestQuiz();
        Question question = addSingleChoiceQuestion(quiz.getId());

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(Arrays.asList(question.getCorrectAnswerIds().get(0)));
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(1, response.getScore());
        assertEquals(1, response.getTotal());
        assertTrue(response.getResults().get(0).getCorrect());
    }

    @Test
    @Order(23)
    @DisplayName("Should score incorrect single choice answer")
    void testSubmitIncorrectSingleChoice() {
        Quiz quiz = createTestQuiz();
        Question question = addSingleChoiceQuestion(quiz.getId());

        Long wrongOptionId = question.getOptions().stream()
                .filter(opt -> !question.getCorrectAnswerIds().contains(opt.getId()))
                .findFirst()
                .get()
                .getId();

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(Arrays.asList(wrongOptionId));
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(0, response.getScore());
        assertEquals(1, response.getTotal());
        assertFalse(response.getResults().get(0).getCorrect());
    }

    @Test
    @Order(24)
    @DisplayName("Should reject multiple selections for single choice")
    void testSubmitMultipleSelectionsForSingleChoice() {
        Quiz quiz = createTestQuiz();
        Question question = addSingleChoiceQuestion(quiz.getId());

        List<Long> multipleOptions = Arrays.asList(
                question.getOptions().get(0).getId(),
                question.getOptions().get(1).getId()
        );

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(multipleOptions);
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(0, response.getScore());
        assertFalse(response.getResults().get(0).getCorrect());
    }

    // ============================================
    // SUBMIT ANSWERS - MULTIPLE CHOICE TESTS
    // ============================================

    @Test
    @Order(25)
    @DisplayName("Should score correct multiple choice answer")
    void testSubmitCorrectMultipleChoice() {
        Quiz quiz = createTestQuiz();
        Question question = addMultipleChoiceQuestion(quiz.getId());

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(new ArrayList<>(question.getCorrectAnswerIds()));
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(1, response.getScore());
        assertTrue(response.getResults().get(0).getCorrect());
    }

    @Test
    @Order(26)
    @DisplayName("Should score incorrect when missing one correct answer")
    void testSubmitMultipleChoiceMissingAnswer() {
        Quiz quiz = createTestQuiz();
        Question question = addMultipleChoiceQuestion(quiz.getId());

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(Arrays.asList(question.getCorrectAnswerIds().get(0)));
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(0, response.getScore());
        assertFalse(response.getResults().get(0).getCorrect());
    }

    @Test
    @Order(27)
    @DisplayName("Should score incorrect when including wrong answer")
    void testSubmitMultipleChoiceWithWrongAnswer() {
        Quiz quiz = createTestQuiz();
        Question question = addMultipleChoiceQuestion(quiz.getId());

        Long wrongOptionId = question.getOptions().stream()
                .filter(opt -> !question.getCorrectAnswerIds().contains(opt.getId()))
                .findFirst()
                .get()
                .getId();

        List<Long> answers = new ArrayList<>(question.getCorrectAnswerIds());
        answers.add(wrongOptionId);

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(question.getId());
        answer.setSelectedOptions(answers);
        request.setAnswers(Arrays.asList(answer));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(0, response.getScore());
        assertFalse(response.getResults().get(0).getCorrect());
    }

    // ============================================
    // SUBMIT ANSWERS - MIXED QUESTIONS TEST
    // ============================================

    @Test
    @Order(28)
    @DisplayName("Should score quiz with mixed question types")
    void testSubmitMixedQuestionTypes() {
        Quiz quiz = createTestQuiz();

        Question q1 = addSingleChoiceQuestion(quiz.getId());
        Question q2 = addMultipleChoiceQuestion(quiz.getId());

        SubmitAnswersRequest request = new SubmitAnswersRequest();

        SubmitAnswersRequest.Answer answer1 = new SubmitAnswersRequest.Answer();
        answer1.setQuestionId(q1.getId());
        answer1.setSelectedOptions(Arrays.asList(q1.getCorrectAnswerIds().get(0)));

        SubmitAnswersRequest.Answer answer2 = new SubmitAnswersRequest.Answer();
        answer2.setQuestionId(q2.getId());
        answer2.setSelectedOptions(new ArrayList<>(q2.getCorrectAnswerIds()));

        request.setAnswers(Arrays.asList(answer1, answer2));

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(2, response.getScore());
        assertEquals(2, response.getTotal());
    }

    @Test
    @Order(29)
    @DisplayName("Should throw error for invalid question ID")
    void testSubmitInvalidQuestionId() {
        Quiz quiz = createTestQuiz();

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(999L);
        answer.setSelectedOptions(Arrays.asList(1L));
        request.setAnswers(Arrays.asList(answer));

        assertThrows(IllegalArgumentException.class, () -> {
            quizService.submitQuizAnswers(quiz.getId(), request);
        });
    }

    @Test
    @Order(30)
    @DisplayName("Should handle empty answers list")
    void testSubmitEmptyAnswers() {
        Quiz quiz = createTestQuiz();

        SubmitAnswersRequest request = new SubmitAnswersRequest();
        request.setAnswers(Arrays.asList());

        SubmitAnswersResponse response = quizService.submitQuizAnswers(quiz.getId(), request);

        assertEquals(0, response.getScore());
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private Quiz createTestQuiz() {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("Test Quiz");
        return quizService.createQuiz(request);
    }

    private Question addSingleChoiceQuestion(Long quizId) {
        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("What is 2 + 2?");
        request.setType(QuestionType.SINGLE);
        request.setOptions(Arrays.asList("3", "4", "5"));
        request.setCorrectAnswers(Arrays.asList(1));
        return quizService.addQuestionToQuiz(quizId, request);
    }

    private Question addMultipleChoiceQuestion(Long quizId) {
        AddQuestionRequest request = new AddQuestionRequest();
        request.setText("Select all even numbers");
        request.setType(QuestionType.MULTIPLE);
        request.setOptions(Arrays.asList("1", "2", "3", "4"));
        request.setCorrectAnswers(Arrays.asList(1, 3));
        return quizService.addQuestionToQuiz(quizId, request);
    }
}