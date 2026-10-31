package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/*
 * PATTERN: Builder for a class hierarchy (Lombok @SuperBuilder).
 * PATTERN: Template Method, in validate().
 * PRINCIPLES: Liskov Substitution, Interface Segregation.
 *
 * BEFORE (CES): 13 unrelated model classes (VideoAssessmentQuestionModel, TextQuestionModel,
 *   MultipleQuestionModel, ...) with no common base, plus one fat Mongo document holding every
 *   field. updateQuestionDocument had to null out "settings", "maxRetakes", "options"... for the
 *   types they did not apply to.
 * NOW: a small base with only what EVERY question has (id, type, text, required). Each
 *   subclass adds only its own fields: media gets retakes/duration, choice gets options.
 *   Nothing has a field it must ignore (Interface Segregation).
 *
 * @SuperBuilder: a plain @Builder cannot see parent fields, so
 *   MediaQuestion.builder().text(...) would not compile. @SuperBuilder generates builders that
 *   inherit each other, so a subclass builder sets base fields too.
 *
 * Validation: Lombok owns build(), so validation is a separate validate() step that
 *   QuestionFactory always calls right after build(). validate() is a Template Method: the base
 *   checks common rules, then calls the subclass hook validateSpecific().
 *
 * Liskov: any Question works wherever a Question is expected. branchTargets() has a safe
 *   default (no targets) instead of a method that throws "not supported" on non-choice types.
 *
 * The protected no-arg constructor exists only so Spring Data can rehydrate documents. Fields
 * stay private with getters only, so application code still cannot mutate a question.
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Question {

    private String id;
    private QuestionType type;
    private String text;
    private boolean required;

    public final void validate() {
        if (id == null || id.isBlank()) {
            throw new InvalidQuestionnaireException("question id is required");
        }
        if (type == null) {
            throw new InvalidQuestionnaireException("question type is required");
        }
        if (text == null || text.isBlank()) {
            throw new InvalidQuestionnaireException("question text is required");
        }
        validateSpecific();
    }

    protected abstract void validateSpecific();

    /* Ids of the questions this one can jump to. Only choice questions have any. */
    public List<String> branchTargets() {
        return List.of();
    }

    protected void requireTypeIn(List<QuestionType> allowed) {
        if (!allowed.contains(type)) {
            throw new InvalidQuestionnaireException(getClass().getSimpleName() + " cannot have type " + type);
        }
    }
}
