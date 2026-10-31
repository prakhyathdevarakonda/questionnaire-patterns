package com.example.questionnaire.factory;

import com.example.questionnaire.api.CreateQuestionnaireRequest;
import com.example.questionnaire.api.QuestionRequest;
import com.example.questionnaire.api.QuestionRequest.OptionRequest;
import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.QuestionnaireType;
import com.example.questionnaire.domain.question.MediaQuestion;
import com.example.questionnaire.domain.question.Question;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* Plain unit tests, no Spring or Mongo: the patterns make the rules testable in isolation. */
class QuestionnaireFactoriesTest {

    private final QuestionnaireFactoryProvider provider = new QuestionnaireFactoryProvider(List.of(
            new StandardQuestionnaireFactory(),
            new ConversationalQuestionnaireFactory(),
            new PreQualificationQuestionnaireFactory()));

    @Test
    void builderFailsFastOnMissingName() {
        assertThatThrownBy(() -> Questionnaire.builder().type(QuestionnaireType.STANDARD).createdBy("u1").build())
                .isInstanceOf(InvalidQuestionnaireException.class)
                .hasMessageContaining("name");
    }

    @Test
    void providerRejectsMissingFamily() {
        assertThatThrownBy(() -> new QuestionnaireFactoryProvider(List.of(new StandardQuestionnaireFactory())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mediaQuestionInheritsQuestionnaireDefaults() {
        var factory = provider.forType(QuestionnaireType.STANDARD);
        var q = factory.createQuestionnaire(new CreateQuestionnaireRequest("Std", QuestionnaireType.STANDARD, "u1", 3, 90));

        Question video = factory.createQuestion(q, null, question(QuestionType.VIDEO, null));

        assertThat(video).isInstanceOf(MediaQuestion.class);
        assertThat(((MediaQuestion) video).getMaxRetakes()).isEqualTo(3);
        assertThat(((MediaQuestion) video).getDurationLimitSec()).isEqualTo(90);
    }

    @Test
    void preQualificationRejectsVideoQuestion() {
        var factory = provider.forType(QuestionnaireType.PRE_QUALIFICATION);
        var q = create(QuestionnaireType.PRE_QUALIFICATION);

        assertThatThrownBy(() -> factory.createQuestion(q, null, question(QuestionType.VIDEO, null)))
                .isInstanceOf(InvalidQuestionnaireException.class)
                .hasMessageContaining("do not allow VIDEO");
    }

    @Test
    void standardRejectsBranching() {
        var factory = provider.forType(QuestionnaireType.STANDARD);
        var q = addQuestion(create(QuestionnaireType.STANDARD), factory, question(QuestionType.TEXT, null));
        String textId = q.getQuestions().get(0).getId();
        var withBranch = q.withQuestion(factory.createQuestion(q, null, question(QuestionType.MULTIPLE_CHOICE, textId)));

        assertThatThrownBy(() -> factory.branchingPolicy().validate(withBranch))
                .isInstanceOf(InvalidQuestionnaireException.class)
                .hasMessageContaining("do not support branching");
    }

    @Test
    void conversationalAllowsForwardBranchOnlyAndBlocksLoops() {
        var factory = provider.forType(QuestionnaireType.CONVERSATIONAL);
        var q = create(QuestionnaireType.CONVERSATIONAL);
        q = addQuestion(q, factory, question(QuestionType.SINGLE_CHOICE, null));
        q = addQuestion(q, factory, question(QuestionType.TEXT, null));
        String first = q.getQuestions().get(0).getId();
        String second = q.getQuestions().get(1).getId();

        var forward = q.withReplacedQuestion(factory.createQuestion(q, first, question(QuestionType.SINGLE_CHOICE, second)));
        factory.branchingPolicy().validate(forward);

        var q3 = addQuestion(q, factory, question(QuestionType.YES_NO, null));
        String third = q3.getQuestions().get(2).getId();
        var backward = q3.withReplacedQuestion(factory.createQuestion(q3, third, question(QuestionType.YES_NO, first)));
        assertThatThrownBy(() -> factory.branchingPolicy().validate(backward))
                .isInstanceOf(InvalidQuestionnaireException.class)
                .hasMessageContaining("only branch forward");
    }

    @Test
    void preQualificationPublishRequiresKnockout() {
        var factory = provider.forType(QuestionnaireType.PRE_QUALIFICATION);
        var q = addQuestion(create(QuestionnaireType.PRE_QUALIFICATION), factory, question(QuestionType.NUMBER, null));

        assertThatThrownBy(() -> factory.publishValidator().validate(q))
                .hasMessageContaining("KNOCKOUT");

        var withKnockout = addQuestion(q, factory, question(QuestionType.KNOCKOUT, null));
        factory.publishValidator().validate(withKnockout);
    }

    @Test
    void publishedQuestionnaireIsReadOnly() {
        var factory = provider.forType(QuestionnaireType.STANDARD);
        var published = addQuestion(create(QuestionnaireType.STANDARD), factory, question(QuestionType.TEXT, null)).published();

        assertThatThrownBy(() -> addQuestion(published, factory, question(QuestionType.TEXT, null)))
                .hasMessageContaining("published");
    }

    private Questionnaire create(QuestionnaireType type) {
        return provider.forType(type).createQuestionnaire(new CreateQuestionnaireRequest(type + " q", type, "u1", null, null));
    }

    private static Questionnaire addQuestion(Questionnaire q, QuestionnaireFactory factory, QuestionRequest request) {
        return q.withQuestion(factory.createQuestion(q, null, request));
    }

    /* Builds a valid request of the given type; branchTo sets nextQuestionId on the first option. */
    private static QuestionRequest question(QuestionType type, String branchTo) {
        List<OptionRequest> options = switch (type) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE, DROPDOWN, YES_NO ->
                    List.of(new OptionRequest("Yes", branchTo), new OptionRequest("No", null));
            default -> null;
        };
        return new QuestionRequest(type, "Question " + type, true, null, null, null, null, null, options, null);
    }
}
