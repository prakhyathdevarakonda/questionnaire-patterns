package com.example.questionnaire.factory;

import com.example.questionnaire.api.QuestionRequest;
import com.example.questionnaire.domain.QuestionType;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.ChoiceQuestion;
import com.example.questionnaire.domain.question.KnockoutQuestion;
import com.example.questionnaire.domain.question.MediaQuestion;
import com.example.questionnaire.domain.question.NumberQuestion;
import com.example.questionnaire.domain.question.Option;
import com.example.questionnaire.domain.question.Question;
import com.example.questionnaire.domain.question.TextQuestion;

import java.util.List;
import java.util.UUID;

/*
 * PATTERN: (Simple) Factory, for "which Question class do I build for this QuestionType?".
 *
 * BEFORE (CES): QuestionnaireUtils.createQuestionDocumentFromRequest, a ~150-line
 *     if (VIDEO.equalsIgnoreCase(type)) {...} else if (AUDIO...) {...} else if ...
 *   Copy-pasted media defaults, and an unknown type silently produced a null document.
 * NOW: one exhaustive `switch` over the enum. Each branch only picks the class and maps
 *   its fields; the Builders do the construction and validate() enforces the rules.
 *   Media questions still inherit retakes/duration from the questionnaire, as in CES.
 *
 * Factory vs Abstract Factory: this class answers "WHICH CLASS for this question type?". It
 *   does not know about families. Which types a family ALLOWS is the Abstract Factory's job,
 *   and it calls this class only after that check passes.
 *
 * ponytail: a switch is fine for 9 types that change rarely, and the compiler flags a new enum
 *   value until it is handled. If question types ever become plugins, replace the switch with
 *   a Map<QuestionType, QuestionCreator> filled from Spring beans.
 */
final class QuestionFactory {

    private static final List<String> YES_NO_LABELS = List.of("Yes", "No");

    private QuestionFactory() {
    }

    static Question create(QuestionRequest request, String questionId, Questionnaire owner) {
        Question question = switch (request.type()) {
            case VIDEO, AUDIO -> MediaQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(request.required())
                    .maxRetakes(orDefault(request.maxRetakes(), owner.getDefaultMaxRetakes()))
                    .durationLimitSec(orDefault(request.durationLimitSec(), owner.getDefaultDurationLimitSec()))
                    .build();
            case TEXT -> TextQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(request.required())
                    .maxCharacters(request.maxCharacters())
                    .build();
            case NUMBER -> NumberQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(request.required())
                    .min(request.min()).max(request.max())
                    .build();
            case SINGLE_CHOICE, MULTIPLE_CHOICE, DROPDOWN -> ChoiceQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(request.required())
                    .options(toOptions(request.options()))
                    .build();
            case YES_NO -> ChoiceQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(request.required())
                    .options(request.options() == null || request.options().isEmpty()
                            ? YES_NO_LABELS.stream().map(label -> new Option(newId(), label, null)).toList()
                            : toOptions(request.options()))
                    .build();
            case KNOCKOUT -> KnockoutQuestion.builder()
                    .id(questionId).type(request.type()).text(request.text()).required(true)
                    .mustAnswerYes(request.mustAnswerYes() == null || request.mustAnswerYes())
                    .build();
        };
        question.validate();
        return question;
    }

    static String newId() {
        return UUID.randomUUID().toString();
    }

    private static List<Option> toOptions(List<QuestionRequest.OptionRequest> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream().map(o -> new Option(newId(), o.label(), o.nextQuestionId())).toList();
    }

    private static int orDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
