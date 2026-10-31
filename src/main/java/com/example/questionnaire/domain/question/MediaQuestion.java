package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/*
 * VIDEO and AUDIO share one class because they share the same data (retakes, duration).
 * In CES these were three near-identical classes (Video, Audio, VideoOrAudio QuestionModel)
 * with copy-pasted "inherit retakes/duration from questionnaire" code. Here that inheritance
 * happens once, in QuestionFactory.
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaQuestion extends Question {

    private int maxRetakes;
    private int durationLimitSec;

    @Override
    protected void validateSpecific() {
        requireTypeIn(List.of(QuestionType.VIDEO, QuestionType.AUDIO));
        if (maxRetakes < 0) {
            throw new InvalidQuestionnaireException("maxRetakes must be >= 0");
        }
        if (durationLimitSec <= 0) {
            throw new InvalidQuestionnaireException("durationLimitSec must be > 0");
        }
    }
}
