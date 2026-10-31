package com.example.questionnaire.factory;

import com.example.questionnaire.api.CreateQuestionnaireRequest;
import com.example.questionnaire.api.QuestionRequest;
import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.Question;

import java.util.Set;

/*
 * Shared steps for all families, so each concrete factory only declares what is DIFFERENT
 * about it (type, allowed question types, strategies). That keeps them a few lines each.
 *
 * PATTERN: Template Method. createQuestion() fixes the order of steps (check type allowed,
 *   then build) and the subclass fills in the hook allowedQuestionTypes().
 *
 * Trap avoided: this class never asks `if (this instanceof ConversationalQuestionnaireFactory)`.
 *   Doing that would quietly undo the whole point of polymorphism.
 */
abstract class AbstractQuestionnaireFactory implements QuestionnaireFactory {

    protected abstract Set<QuestionType> allowedQuestionTypes();

    @Override
    public Questionnaire createQuestionnaire(CreateQuestionnaireRequest request) {
        Questionnaire.Builder builder = Questionnaire.builder()
                .name(request.name())
                .type(type())
                .createdBy(request.createdBy());
        if (request.defaultMaxRetakes() != null) {
            builder.defaultMaxRetakes(request.defaultMaxRetakes());
        }
        if (request.defaultDurationLimitSec() != null) {
            builder.defaultDurationLimitSec(request.defaultDurationLimitSec());
        }
        return builder.build();
    }

    @Override
    public final Question createQuestion(Questionnaire owner, String questionId, QuestionRequest request) {
        if (request.type() == null) {
            throw new InvalidQuestionnaireException("question type is required");
        }
        if (!allowedQuestionTypes().contains(request.type())) {
            throw new InvalidQuestionnaireException(type() + " questionnaires do not allow " + request.type()
                    + " questions; allowed: " + allowedQuestionTypes());
        }
        return QuestionFactory.create(request, questionId == null ? QuestionFactory.newId() : questionId, owner);
    }
}
