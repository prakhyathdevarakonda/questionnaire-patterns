package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/*
 * A yes/no question where the wrong answer disqualifies the candidate.
 *
 * PRINCIPLE: Liskov Substitution. It looks like a YES_NO ChoiceQuestion, so it is tempting to
 * write `KnockoutQuestion extends ChoiceQuestion`. But its meaning differs: one answer ends
 * the screening. Code that treats a ChoiceQuestion as "just pick an option" would quietly
 * ignore that. So it is a sibling (extends Question), not a subclass of ChoiceQuestion.
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnockoutQuestion extends Question {

    /* true: the candidate must answer "Yes" to continue; false: must answer "No". */
    private boolean mustAnswerYes;

    @Override
    protected void validateSpecific() {
        requireTypeIn(List.of(QuestionType.KNOCKOUT));
    }
}
