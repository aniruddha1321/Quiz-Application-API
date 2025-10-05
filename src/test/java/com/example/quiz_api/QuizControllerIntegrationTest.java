package com.example.quiz_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.quiz_api.dto.*;
import com.example.quiz_api.model.QuestionType;
import com.example.quiz_api.repository.QuizRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for QuizController
 * Tests complete request-response cycle for all endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository repository;

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/quizzes - Should create quiz")
    void testCreateQuiz() throws Exception {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("Java Quiz");

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("Java Quiz"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/quizzes - Should return all quizzes")
    void testGetAllQuizzes() throws Exception {
        CreateQuizRequest request1 = new CreateQuizRequest();
        request1.setTitle("Quiz 1");
        CreateQuizRequest request2 = new CreateQuizRequest();
        request2.setTitle("Quiz 2");

        mockMvc.perform(post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].questionCount").exists());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/quizzes/{id}/questions - Should add question")
    void testAddQuestion() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Test Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("What is Java?");
        questionRequest.setType(QuestionType.SINGLE);
        questionRequest.setOptions(Arrays.asList("Language", "Framework", "Database"));
        questionRequest.setCorrectAnswers(Arrays.asList(0));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.text").value("What is Java?"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/quizzes/{id}/questions - Should return questions without answers")
    void testGetQuizQuestions() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Test Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("Test Question?");
        questionRequest.setType(QuestionType.SINGLE);
        questionRequest.setOptions(Arrays.asList("A", "B"));
        questionRequest.setCorrectAnswers(Arrays.asList(0));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequest)));

        mockMvc.perform(get("/api/quizzes/" + quizId + "/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].text").exists())
                .andExpect(jsonPath("$.data[0].correctAnswerIds").doesNotExist());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/quizzes/{id}/submit - Should submit answers and get score")
    void testSubmitAnswers() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Test Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("What is 2 + 2?");
        questionRequest.setType(QuestionType.SINGLE);
        questionRequest.setOptions(Arrays.asList("3", "4", "5"));
        questionRequest.setCorrectAnswers(Arrays.asList(1));

        String questionResponse = mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long questionId = objectMapper.readTree(questionResponse)
                .get("data").get("id").asLong();

        Long correctOptionId = objectMapper.readTree(questionResponse)
                .get("data").get("options").get(1).get("id").asLong();

        SubmitAnswersRequest submitRequest = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(questionId);
        answer.setSelectedOptions(Arrays.asList(correctOptionId));
        submitRequest.setAnswers(Arrays.asList(answer));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.results[0].correct").value(true));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/quizzes/{id}/submit - Should handle incorrect answer")
    void testSubmitIncorrectAnswer() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Test Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("What is 2 + 2?");
        questionRequest.setType(QuestionType.SINGLE);
        questionRequest.setOptions(Arrays.asList("3", "4", "5"));
        questionRequest.setCorrectAnswers(Arrays.asList(1));

        String questionResponse = mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long questionId = objectMapper.readTree(questionResponse)
                .get("data").get("id").asLong();

        Long wrongOptionId = objectMapper.readTree(questionResponse)
                .get("data").get("options").get(0).get("id").asLong();

        SubmitAnswersRequest submitRequest = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(questionId);
        answer.setSelectedOptions(Arrays.asList(wrongOptionId));
        submitRequest.setAnswers(Arrays.asList(answer));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.results[0].correct").value(false));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/quizzes/{id}/questions - Should return 404 for non-existent quiz")
    void testGetQuestionsForNonExistentQuiz() throws Exception {
        mockMvc.perform(get("/api/quizzes/999/questions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/quizzes/{id}/questions - Should validate question data")
    void testAddQuestionValidation() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Test Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        // Invalid question: single choice with no correct answer
        AddQuestionRequest invalidRequest = new AddQuestionRequest();
        invalidRequest.setText("Invalid Question?");
        invalidRequest.setType(QuestionType.SINGLE);
        invalidRequest.setOptions(Arrays.asList("A", "B"));
        invalidRequest.setCorrectAnswers(Arrays.asList());

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/quizzes/{id}/submit - Should validate quiz belongs to question")
    void testSubmitAnswerValidatesQuizId() throws Exception {
        // Create two quizzes
        CreateQuizRequest quiz1Request = new CreateQuizRequest();
        quiz1Request.setTitle("Quiz 1");

        String quiz1Response = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz1Request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quiz1Id = objectMapper.readTree(quiz1Response)
                .get("data").get("id").asLong();

        CreateQuizRequest quiz2Request = new CreateQuizRequest();
        quiz2Request.setTitle("Quiz 2");

        String quiz2Response = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz2Request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quiz2Id = objectMapper.readTree(quiz2Response)
                .get("data").get("id").asLong();

        // Add question to quiz 1
        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("Question?");
        questionRequest.setType(QuestionType.SINGLE);
        questionRequest.setOptions(Arrays.asList("A", "B"));
        questionRequest.setCorrectAnswers(Arrays.asList(0));

        String questionResponse = mockMvc.perform(post("/api/quizzes/" + quiz1Id + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long questionId = objectMapper.readTree(questionResponse)
                .get("data").get("id").asLong();

        Long optionId = objectMapper.readTree(questionResponse)
                .get("data").get("options").get(0).get("id").asLong();

        // Try to submit answer for quiz 2 using question from quiz 1
        SubmitAnswersRequest submitRequest = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(questionId);
        answer.setSelectedOptions(Arrays.asList(optionId));
        submitRequest.setAnswers(Arrays.asList(answer));

        mockMvc.perform(post("/api/quizzes/" + quiz2Id + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("does not belong")));
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/quizzes - Should handle multiple choice questions")
    void testMultipleChoiceQuestion() throws Exception {
        CreateQuizRequest quizRequest = new CreateQuizRequest();
        quizRequest.setTitle("Multiple Choice Quiz");

        String quizResponse = mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quizId = objectMapper.readTree(quizResponse)
                .get("data").get("id").asLong();

        AddQuestionRequest questionRequest = new AddQuestionRequest();
        questionRequest.setText("Select all even numbers");
        questionRequest.setType(QuestionType.MULTIPLE);
        questionRequest.setOptions(Arrays.asList("1", "2", "3", "4"));
        questionRequest.setCorrectAnswers(Arrays.asList(1, 3));

        String questionResponse = mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long questionId = objectMapper.readTree(questionResponse)
                .get("data").get("id").asLong();

        Long option2Id = objectMapper.readTree(questionResponse)
                .get("data").get("options").get(1).get("id").asLong();

        Long option4Id = objectMapper.readTree(questionResponse)
                .get("data").get("options").get(3).get("id").asLong();

        SubmitAnswersRequest submitRequest = new SubmitAnswersRequest();
        SubmitAnswersRequest.Answer answer = new SubmitAnswersRequest.Answer();
        answer.setQuestionId(questionId);
        answer.setSelectedOptions(Arrays.asList(option2Id, option4Id));
        submitRequest.setAnswers(Arrays.asList(answer));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1))
                .andExpect(jsonPath("$.data.results[0].correct").value(true));
    }
}