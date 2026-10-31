package com.example.questionnaire.factory;

import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.QuestionnaireType;
import com.example.questionnaire.strategy.BranchingPolicy;
import com.example.questionnaire.strategy.ForwardOnlyBranchingPolicy;
import com.example.questionnaire.strategy.NonEmptyPublishValidator;
import com.example.questionnaire.strategy.PublishValidator;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/*
 * Concrete family: a chat-like flow. No media; choice answers may branch forward.
 * The only line that differs from Standard in behaviour is the BranchingPolicy it returns.
 * That is Strategy doing its job: same flow, different plugged-in behaviour.
 */
@Component
public class ConversationalQuestionnaireFactory extends AbstractQuestionnaireFactory {

    private static final Set<QuestionType> ALLOWED = EnumSet.of(
            QuestionType.TEXT, QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE, QuestionType.YES_NO);

    private final BranchingPolicy branchingPolicy = new ForwardOnlyBranchingPolicy();
    private final PublishValidator publishValidator = new NonEmptyPublishValidator();

    @Override
    public QuestionnaireType type() {
        return QuestionnaireType.CONVERSATIONAL;
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
