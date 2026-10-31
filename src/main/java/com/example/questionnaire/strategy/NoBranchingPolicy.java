package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.Question;

/*
 * "Branching is off" is a real policy with real behaviour (reject branches), not a missing one.
 * Returning this instead of null or a throwing stub keeps every factory interchangeable (Liskov).
 */
public class NoBranchingPolicy implements BranchingPolicy {

    @Override
    public void validate(Questionnaire questionnaire) {
        for (Question question : questionnaire.getQuestions()) {
            if (!question.branchTargets().isEmpty()) {
                throw new InvalidQuestionnaireException(
                        questionnaire.getType() + " questionnaires do not support branching (question " + question.getId() + ")");
            }
        }
    }
}
