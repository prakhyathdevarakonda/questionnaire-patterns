package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.Questionnaire;

public class NonEmptyPublishValidator implements PublishValidator {

    @Override
    public void validate(Questionnaire questionnaire) {
        if (questionnaire.getQuestions().isEmpty()) {
            throw new InvalidQuestionnaireException("cannot publish a questionnaire without questions");
        }
    }
}
