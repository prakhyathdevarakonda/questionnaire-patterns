package com.example.questionnaire.domain.question;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/* maxCharacters == null means unlimited (CES encoded this as the magic number -1). */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextQuestion extends Question {

    private Integer maxCharacters;

    @Override
    protected void validateSpecific() {
        requireTypeIn(List.of(QuestionType.TEXT));
        if (maxCharacters != null && maxCharacters <= 0) {
            throw new InvalidQuestionnaireException("maxCharacters must be > 0 when set");
        }
    }
}
