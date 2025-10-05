package com.example.quiz_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Entry Point
 * This class bootstraps the entire Quiz API application
 */
@SpringBootApplication
public class QuizApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizApiApplication.class, args);
		System.out.println("\n===========================================");
		System.out.println("Quiz API Application Started Successfully!");
		System.out.println("Server running at: http://localhost:8080");
		System.out.println("===========================================\n");
	}
}