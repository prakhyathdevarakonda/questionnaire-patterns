package com.example.questionnaire.factory;

import com.example.questionnaire.api.CreateQuestionnaireRequest;
import com.example.questionnaire.api.QuestionRequest;
import com.example.questionnaire.domain.QuestionnaireType;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.Question;
import com.example.questionnaire.strategy.BranchingPolicy;
import com.example.questionnaire.strategy.PublishValidator;

/*
 * PATTERN: Abstract Factory, an interface for creating a FAMILY of related objects that must
 * be used together, without the caller naming concrete classes.
 *
 * The family here is one questionnaire type, and its products are:
 *   1. the Questionnaire itself
 *   2. the Questions it is allowed to contain
 *   3. its BranchingPolicy  (Strategy)
 *   4. its PublishValidator (Strategy)
 * Mixing products from different families (a pre-qualification questionnaire with conversational
 * branching, or with VIDEO questions) is the bug this pattern prevents: you get all four from
 * the same factory, so they always match.
 *
 * BEFORE (CES): the family was re-derived at every step from the screeningType string:
 *   createQuestionV2 -> if (WORKFLOW) ... else if (CONVERSATIONAL) ... else ...
 *   createQuestion   -> if (!WORKFLOW || TEXT_BLOCK) ...
 *   autoPublish      -> if (WORKFLOW && !isValidWorkFlowQuestionnaireForPublish) ...
 * NOW: look the factory up once (QuestionnaireFactoryProvider.forType), then only ask IT.
 *
 * Why Abstract Factory and not a plain Factory: a plain factory would only answer
 * "create questionnaire of type X". Here several related things vary TOGETHER per family,
 * which is the exact situation Abstract Factory is for. If the only difference were the allowed
 * question types, a Set<QuestionType> per type would be enough and this would be over-design.
 *
 * PRINCIPLE: Dependency Inversion. QuestionnaireService depends on this interface, never on
 * StandardQuestionnaireFactory and friends.
 */
public interface QuestionnaireFactory {

    QuestionnaireType type();

    Questionnaire createQuestionnaire(CreateQuestionnaireRequest request);

    /* questionId == null creates a new question; otherwise rebuilds that question (used by update). */
    Question createQuestion(Questionnaire owner, String questionId, QuestionRequest request);

    BranchingPolicy branchingPolicy();

    PublishValidator publishValidator();
}
