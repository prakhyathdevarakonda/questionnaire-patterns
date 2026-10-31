package com.example.questionnaire.factory;

import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.QuestionnaireType;
import com.example.questionnaire.strategy.BranchingPolicy;
import com.example.questionnaire.strategy.KnockoutRequiredPublishValidator;
import com.example.questionnaire.strategy.NoBranchingPolicy;
import com.example.questionnaire.strategy.PublishValidator;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/*
 * Concrete family: quick eligibility screening before the real interview. Short, closed answers
 * only, at least one knockout question, no branching.
 *
 * In CES this family was not a type at all; it was a job pre-qualification Kafka event sitting
 * next to the "workflow" screening type. Here it is a first-class family like the other two.
 */
@Component
public class PreQualificationQuestionnaireFactory extends AbstractQuestionnaireFactory {

    private static final Set<QuestionType> ALLOWED = EnumSet.of(
            QuestionType.YES_NO, QuestionType.KNOCKOUT, QuestionType.NUMBER, QuestionType.DROPDOWN);

    private final BranchingPolicy branchingPolicy = new NoBranchingPolicy();
    private final PublishValidator publishValidator = new KnockoutRequiredPublishValidator();

    @Override
    public QuestionnaireType type() {
        return QuestionnaireType.PRE_QUALIFICATION;
    }

    @Override
    protected Set<QuestionType> allowedQuestionTypes() {
        return ALLOWED;
    }

    @Override
    public BranchingPolicy branchingPolicy() {
        return branchingPolicy;
    }

    @Override
    public PublishValidator publishValidator() {
        return publishValidator;
    }
}
