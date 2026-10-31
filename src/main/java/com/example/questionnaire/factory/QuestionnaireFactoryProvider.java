package com.example.questionnaire.factory;

import com.example.questionnaire.domain.InvalidQuestionnaireException;
import com.example.questionnaire.domain.QuestionnaireType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/*
 * Picks the right Abstract Factory for a QuestionnaireType.
 *
 * Spring injects EVERY QuestionnaireFactory bean as a List, and we index them by type().
 * There is no switch here, so adding a fourth family means writing one new @Component and
 * touching nothing else. That is the Open/Closed principle in practice.
 *
 * Fail fast at startup: a type with no factory, or two factories for one type, stops the app
 * from booting instead of failing on the first request that hits it.
 */
@Component
public class QuestionnaireFactoryProvider {

    private final Map<QuestionnaireType, QuestionnaireFactory> factories = new EnumMap<>(QuestionnaireType.class);

    public QuestionnaireFactoryProvider(List<QuestionnaireFactory> all) {
        for (QuestionnaireFactory factory : all) {
            if (factories.put(factory.type(), factory) != null) {
                throw new IllegalStateException("more than one factory for " + factory.type());
            }
        }
        for (QuestionnaireType type : QuestionnaireType.values()) {
            if (!factories.containsKey(type)) {
                throw new IllegalStateException("no QuestionnaireFactory for " + type);
            }
        }
    }

    public QuestionnaireFactory forType(QuestionnaireType type) {
        if (type == null) {
            throw new InvalidQuestionnaireException("questionnaire type is required");
        }
        return factories.get(type);
    }
}
