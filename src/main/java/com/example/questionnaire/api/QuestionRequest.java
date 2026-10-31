package com.example.questionnaire.api;

import com.example.questionnaire.domain.QuestionType;

import java.util.List;

/*
 * One flat request for every question type, because that is what a UI form naturally sends.
 * QuestionFactory reads only the fields that matter for `type`; the domain objects it builds are
 * NOT flat (see Question and its subclasses).
 */
public record QuestionRequest(
        QuestionType type,
        String text,
        boolean required,
        Integer maxRetakes,
        Integer durationLimitSec,
        Integer maxCharacters,
        Integer min,
        Integer max,
        List<OptionRequest> options,
        Boolean mustAnswerYes) {

    public record OptionRequest(String label, String nextQuestionId) {
    }
}
