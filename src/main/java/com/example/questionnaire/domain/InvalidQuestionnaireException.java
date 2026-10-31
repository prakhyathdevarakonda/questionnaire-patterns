package com.example.questionnaire.domain;

/*
 * PRINCIPLE: Fail fast, with a reason.
 *
 * BEFORE (CES): invalid input usually produced `CompletableFuture.completedFuture(null)`,
 *   so the caller saw a null/500 and had to read logs to learn why.
 * NOW: the rule that was broken throws this with a human-readable message; the API layer
 *   turns it into a 400 with that message.
 */
public class InvalidQuestionnaireException extends RuntimeException {

    public InvalidQuestionnaireException(String message) {
        super(message);
    }
}
