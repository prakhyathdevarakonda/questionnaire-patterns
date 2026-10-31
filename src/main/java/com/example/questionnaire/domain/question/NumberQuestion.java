package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NumberQuestion extends Question {

    private Integer min;
    private Integer max;

    @Override
    protected void validateSpecific() {
        requireTypeIn(List.of(QuestionType.NUMBER));
        if (min != null && max != null && min > max) {
            throw new InvalidQuestionnaireException("min must be <= max");
        }
    }
}
