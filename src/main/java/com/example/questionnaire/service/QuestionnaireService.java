package com.example.questionnaire.service;

import com.example.questionnaire.api.CreateQuestionnaireRequest;
import com.example.questionnaire.api.QuestionRequest;
import com.example.questionnaire.domain.NotFoundException;
import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.domain.question.Question;
import com.example.questionnaire.factory.QuestionnaireFactory;
import com.example.questionnaire.factory.QuestionnaireFactoryProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * PRINCIPLE: Single Responsibility. The service only orchestrates: load, ask the family's
 *   factory and strategies, save. It contains no type-specific rule. Search it for
 *   "CONVERSATIONAL" or "KNOCKOUT" and you will find nothing.
 *
 * BEFORE (CES): VideoQuestionnaireService (~5,900 lines) mixed HTTP payload parsing, user
 *   lookup, locale resolution, Mongo/Redis calls and per-type branching in the same methods.
 * NOW: each of those concerns has its own class; this one reads top to bottom in a minute.
 *
 * Constructor injection (instead of CES's field @Inject): dependencies are visible, final, and
 * a test can pass fakes without Spring.
 */
@Service
public class QuestionnaireService {

    private final QuestionnaireRepository repository;
    private final QuestionnaireFactoryProvider factories;

    public QuestionnaireService(QuestionnaireRepository repository, QuestionnaireFactoryProvider factories) {
        this.repository = repository;
        this.factories = factories;
    }

    public Questionnaire create(CreateQuestionnaireRequest request) {
        Questionnaire questionnaire = factories.forType(request.type()).createQuestionnaire(request);
        return repository.save(questionnaire);
    }

    public Questionnaire get(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("questionnaire " + id + " not found"));
    }

    public List<Questionnaire> list() {
        return repository.findAll();
    }

    public Questionnaire addQuestion(String questionnaireId, QuestionRequest request) {
        Questionnaire questionnaire = get(questionnaireId);
        QuestionnaireFactory factory = factories.forType(questionnaire.getType());
        Question question = factory.createQuestion(questionnaire, null, request);
        return saveValidated(factory, questionnaire.withQuestion(question));
    }

    public Questionnaire replaceQuestion(String questionnaireId, String questionId, QuestionRequest request) {
        Questionnaire questionnaire = get(questionnaireId);
        if (!questionnaire.containsQuestion(questionId)) {
            throw new NotFoundException("question " + questionId + " not found");
        }
        QuestionnaireFactory factory = factories.forType(questionnaire.getType());
        Question question = factory.createQuestion(questionnaire, questionId, request);
        return saveValidated(factory, questionnaire.withReplacedQuestion(question));
    }

    public Questionnaire publish(String questionnaireId) {
        Questionnaire questionnaire = get(questionnaireId);
        QuestionnaireFactory factory = factories.forType(questionnaire.getType());
        factory.publishValidator().validate(questionnaire);
        return repository.save(questionnaire.published());
    }

    private Questionnaire saveValidated(QuestionnaireFactory factory, Questionnaire updated) {
        factory.branchingPolicy().validate(updated);
        return repository.save(updated);
    }
}
