package com.example.questionnaire.factory;

import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.QuestionnaireType;
import com.example.questionnaire.strategy.BranchingPolicy;
import com.example.questionnaire.strategy.NoBranchingPolicy;
import com.example.questionnaire.strategy.NonEmptyPublishValidator;
import com.example.questionnaire.strategy.PublishValidator;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/*
 * Concrete family: classic video screening. Media plus simple text/choice, a fixed order, no
 * branching.
 */
@Component
public class StandardQuestionnaireFactory extends AbstractQuestionnaireFactory {

    private static final Set<QuestionType> ALLOWED = EnumSet.of(
            QuestionType.VIDEO, QuestionType.AUDIO, QuestionType.TEXT, QuestionType.MULTIPLE_CHOICE);

    private final BranchingPolicy branchingPolicy = new NoBranchingPolicy();
    private final PublishValidator publishValidator = new NonEmptyPublishValidator();

    @Override
    public QuestionnaireType type() {
        return QuestionnaireType.STANDARD;
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
