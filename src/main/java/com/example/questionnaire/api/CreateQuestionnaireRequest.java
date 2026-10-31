package com.example.questionnaire.api;

import com.example.questionnaire.domain.QuestionnaireType;

/* Optional numbers are nullable: null means "use the builder's default". */
public record CreateQuestionnaireRequest(
        String name,
        QuestionnaireType type,
        String createdBy,
        Integer defaultMaxRetakes,
        Integer defaultDurationLimitSec) {
}
