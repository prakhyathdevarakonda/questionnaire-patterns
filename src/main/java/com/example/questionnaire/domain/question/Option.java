package com.example.questionnaire.domain.question;

/* nextQuestionId == null means "continue with the next question in order". */
public record Option(String id, String label, String nextQuestionId) {
}
