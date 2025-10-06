# Quiz Application API Documentation

## Project Description

A RESTful backend API for an online quiz application built with Java and Spring Boot. The API allows users to create quizzes, add different types of questions (single choice, multiple choice, and text-based), and submit answers for automatic scoring.

**Core Features:**
- Create and manage quizzes
- Add three types of questions with validation
- Retrieve quiz questions (without exposing correct answers)
- Submit answers and receive instant scoring
- List all available quizzes

**Technology Stack:**
- Java 17
- Spring Boot 3.2.0
- Maven (Build tool)
- JUnit 5 (Testing framework)
- In-memory storage (ConcurrentHashMap)

---

## Setup and Run Instructions

### Prerequisites

Ensure you have the following installed:
- **Java 17** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))

Verify installation:
```bash
java -version
mvn -version
```

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/quiz-api.git
   cd quiz-api
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```
   This will download all dependencies and compile the project.

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify it's running**

   You should see output like:
   ```
   ===========================================
   Quiz API Application Started Successfully!
   Server running at: http://localhost:8080
   ===========================================
   ```

5. **Test the API**

   Open a new terminal and test the endpoints:
   ```bash
   curl http://localhost:8080/api/quizzes
   ```

### Alternative: Run from IDE

1. Import the project as a Maven project in your IDE (IntelliJ IDEA, Eclipse, or VS Code)
2. Navigate to `src/main/java/com/quiz/QuizApplication.java`
3. Right-click and select "Run" or "Run as Java Application"

---

## Running Test Cases

The project includes comprehensive test coverage with 40+ test cases.

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Run service layer tests
mvn test -Dtest=QuizServiceTest

# Run controller integration tests
mvn test -Dtest=QuizControllerIntegrationTest
```

### Test Coverage

The test suite includes:

**Unit Tests (QuizServiceTest.java)** - 30+ tests covering:
- Quiz creation and management
- Single choice question validation
- Multiple choice question validation
- Text question validation
- Answer submission and scoring
- Edge cases and error handling

**Integration Tests (QuizControllerIntegrationTest.java)** - 10+ tests covering:
- Complete API request/response flow
- All 5 REST endpoints
- HTTP status code validation
- Error handling scenarios

### Expected Test Output

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.quiz.service.QuizServiceTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.quiz.controller.QuizControllerIntegrationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 40+, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## Assumptions and Design Choices

### Assumptions

1. **Storage**: The application uses in-memory storage (no database) as per the project requirements. Data is lost when the application restarts.

2. **Authentication**: No user authentication or authorization is implemented. All endpoints are publicly accessible.

3. **Question Limits**:
    - Text questions have a maximum word limit of 300 characters
    - Single and multiple choice questions require at least 2 options

4. **Answer Matching**:
    - Text answers are case-insensitive ("Paris" matches "paris")
    - Whitespace is trimmed from text answers

5. **Concurrent Access**: The application is designed to handle multiple simultaneous users.

### Design Choices

#### 1. Layered Architecture

The project follows a clean separation of concerns with distinct layers:

- **Controller Layer**: Handles HTTP requests/responses and routing
- **Service Layer**: Contains business logic and validation rules
- **Repository Layer**: Manages data access and storage
- **DTO Layer**: Separates internal models from API contracts

**Rationale**: This design makes the code maintainable, testable, and allows easy replacement of components (e.g., swapping in-memory storage for a database).

#### 2. Thread-Safe Storage

Used `ConcurrentHashMap` and `AtomicLong` for data storage and ID generation.

**Rationale**: Ensures the application can safely handle concurrent requests without data corruption or race conditions.

#### 3. DTO Pattern

Created separate Request and Response DTOs instead of directly exposing domain models.

**Rationale**:
- Hides sensitive data (correct answers are not exposed when fetching questions)
- Allows different representations for input and output
- Enables field-level validation using annotations

#### 4. RESTful API Design

Followed REST principles with proper HTTP methods and status codes:
- `POST` for creation (returns 201 Created)
- `GET` for retrieval (returns 200 OK)
- `400 Bad Request` for validation errors
- `404 Not Found` for missing resources

**Rationale**: Provides a predictable and standard API interface that's easy to consume.

#### 5. Validation Strategy

Implemented two levels of validation:
- **Request-level**: Using Jakarta validation annotations (@NotBlank, @NotNull)
- **Business-level**: Custom validation in the service layer based on question types

**Rationale**: Ensures data integrity while providing clear, specific error messages to API consumers.

#### 6. Question Type Polymorphism

Used an enum (`QuestionType`) to distinguish between question types rather than separate classes.

**Rationale**: Simpler to implement for this use case while still maintaining type safety. Fields like `correctAnswerIds` vs `correctAnswerTexts` are conditionally used based on type.

#### 7. Generic API Response Wrapper

All API responses are wrapped in `ApiResponse<T>` with `success`, `data`, and `error` fields.

**Rationale**: Provides consistent response structure across all endpoints, making client-side error handling easier.

#### 8. Comprehensive Testing

Included extensive unit and integration tests (40+ test cases).

**Rationale**: Ensures code quality, catches regressions early, and documents expected behavior.

#### 9. ID Management

Questions and options are assigned unique IDs by the repository layer.

**Rationale**: Maintains referential integrity and allows for efficient lookups. IDs are used instead of indices to prevent issues when data changes.

#### 10. Separation of Quiz Questions for Display

When fetching questions for quiz takers, correct answers are deliberately excluded.

**Rationale**: Prevents cheating by not exposing answers to quiz takers while still allowing the backend to validate submissions.

---

## API Endpoints Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/quizzes` | Create a new quiz |
| GET | `/api/quizzes` | Get all quizzes |
| POST | `/api/quizzes/{id}/questions` | Add question to quiz |
| GET | `/api/quizzes/{id}/questions` | Get quiz questions |
| POST | `/api/quizzes/{id}/submit` | Submit answers and get score |

---

## Project Structure

```
quiz-api/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/quiz/
    │   │   ├── QuizApplication.java          # Main entry point
    │   │   ├── controller/
    │   │   │   └── QuizController.java       # REST endpoints
    │   │   ├── service/
    │   │   │   └── QuizService.java          # Business logic
    │   │   ├── repository/
    │   │   │   └── QuizRepository.java       # Data access
    │   │   ├── model/
    │   │   │   ├── Quiz.java                 # Domain models
    │   │   │   ├── Question.java
    │   │   │   ├── Option.java
    │   │   │   └── QuestionType.java
    │   │   └── dto/
    │   │       ├── CreateQuizRequest.java    # Request DTOs
    │   │       ├── AddQuestionRequest.java
    │   │       ├── SubmitAnswersRequest.java
    │   │       ├── QuizListResponse.java     # Response DTOs
    │   │       ├── QuestionResponse.java
    │   │       ├── SubmitAnswersResponse.java
    │   │       └── ApiResponse.java
    │   └── resources/
    │       └── application.properties        # Configuration
    └── test/
        ├── java/com/quiz/
        │   ├── QuizApplicationTests.java
        │   ├── service/
        │   │   └── QuizServiceTest.java      # Unit tests
        │   └── controller/
        │       └── QuizControllerIntegrationTest.java  # Integration tests
        └── resources/
            └── application-test.properties
```

---

## Contact

For questions or issues, please open an issue on GitHub or contact [sheteaniruddha0@gmail.com]

---

**Built with Java 17 and Spring Boot 3.2.0**