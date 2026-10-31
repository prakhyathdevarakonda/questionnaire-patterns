package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.Questionnaire;

/*
 * PATTERN: Strategy. "How do we treat branching?" is a behaviour that varies by family, so it
 * sits behind an interface, with one small class per behaviour.
 *
 * BEFORE (CES): branching rules were inline checks spread across VideoQuestionnaireService and
 *   QuestionnaireUtils:
 *     if (!isConversationalQuestionnaire(q) || !isBranchingAllowedForQuestionType(type)) {
 *         if (isQuestionContainsConditionalFlow(payload)) throw ...
 *     }
 *   plus isInvalidBranchingQuestion, isAddOrRemoveConditionalFlowInOptions, ...
 * NOW: the caller just runs factory.branchingPolicy().validate(questionnaire). It never asks
 *   "am I conversational?" (Tell, Don't Ask). New branching rules mean a new class, with no
 *   edits to existing ones (Open/Closed).
 *
 * It validates the WHOLE questionnaire, not one question, because a branch is a relation
 * between two questions and is only meaningful once both are in place.
 */
public interface BranchingPolicy {

    void validate(Questionnaire questionnaire);
}
