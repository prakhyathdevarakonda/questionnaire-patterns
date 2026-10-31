package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.Questionnaire;

/*
 * PRINCIPLE: Composition over inheritance. It reuses the non-empty rule by HOLDING a
 * NonEmptyPublishValidator, not by extending it. Each rule stays a tiny, independent class.
 */
public class KnockoutRequiredPublishValidator implements PublishValidator {

    private final PublishValidator nonEmpty = new NonEmptyPublishValidator();

    @Override
    public void validate(Questionnaire questionnaire) {
        nonEmpty.validate(questionnaire);
        boolean hasKnockout = questionnaire.getQuestions().stream()
                .anyMatch(q -> q.getType() == QuestionType.KNOCKOUT);
        if (!hasKnockout) {
            throw new InvalidQuestionnaireException("a pre-qualification questionnaire needs at least one KNOCKOUT question");
        }
    }
}
