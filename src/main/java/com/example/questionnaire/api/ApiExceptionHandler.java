package com.example.questionnaire.api;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.NotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/* Turns domain failures into RFC 7807 problem responses carrying the broken rule's message. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidQuestionnaireException.class)
    public ProblemDetail invalid(InvalidQuestionnaireException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail conflict(OptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "questionnaire was changed by another request; reload and retry");
    }
}
