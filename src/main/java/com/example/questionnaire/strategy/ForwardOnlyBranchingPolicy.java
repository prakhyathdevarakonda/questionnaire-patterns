package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.Question;

/*
 * Conversational branching: an option may jump to another question, but only to one that
 * exists and comes LATER in the questionnaire. Forward-only jumps cannot form a loop, so
 * every candidate is guaranteed to reach the end.
 *
 * Consequence for the API: a freshly added question is always last, so it cannot branch yet.
 * Add the questions first, then PUT the earlier question with nextQuestionId set (CES works the
 * same way: create questions, then update one with conditionalFlow).
 */
public class ForwardOnlyBranchingPolicy implements BranchingPolicy {

    @Override
    public void validate(Questionnaire questionnaire) {
        for (Question question : questionnaire.getQuestions()) {
            int from = questionnaire.indexOf(question.getId());
            for (String target : question.branchTargets()) {
                int to = questionnaire.indexOf(target);
                if (to < 0) {
                    throw new InvalidQuestionnaireException(
                            "question " + question.getId() + " branches to unknown question " + target);
                }
                if (to <= from) {
                    throw new InvalidQuestionnaireException(
                            "question " + question.getId() + " may only branch forward, not to " + target);
                }
            }
        }
    }
}
