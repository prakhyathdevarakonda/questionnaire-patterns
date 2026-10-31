package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/*
 * SINGLE_CHOICE, MULTIPLE_CHOICE, DROPDOWN and YES_NO all hold the same data: a list of options.
 *
 * Whether an option may branch (nextQuestionId) is NOT decided here. The question only reports
 * its targets; the family's BranchingPolicy (Strategy) decides if that is allowed. The same
 * ChoiceQuestion class is therefore valid in a conversational questionnaire (branching on) and
 * a standard one (branching off), without any if (conversational) inside it.
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceQuestion extends Question {

    private List<Option> options;

    @Override
    protected void validateSpecific() {
        requireTypeIn(List.of(QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE,
                QuestionType.DROPDOWN, QuestionType.YES_NO));
        if (options == null || options.size() < 2) {
            throw new InvalidQuestionnaireException("a choice question needs at least 2 options");
        }
        if (getType() == QuestionType.YES_NO && options.size() != 2) {
            throw new InvalidQuestionnaireException("a yes/no question has exactly 2 options");
        }
        var labels = new HashSet<String>();
        for (Option option : options) {
            if (option.label() == null || option.label().isBlank()) {
                throw new InvalidQuestionnaireException("option label is required");
            }
            if (!labels.add(option.label().trim().toLowerCase())) {
                throw new InvalidQuestionnaireException("duplicate option label: " + option.label());
            }
        }
    }

    @Override
    public List<String> branchTargets() {
        return options.stream().map(Option::nextQuestionId).filter(Objects::nonNull).toList();
    }
}
