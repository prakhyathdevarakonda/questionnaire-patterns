package com.example.questionnaire.api;

import com.example.questionnaire.domain.Questionnaire;
import com.example.questionnaire.service.QuestionnaireService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* Only maps HTTP to service calls. No rules live here (Single Responsibility). */
@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService service;

    public QuestionnaireController(QuestionnaireService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Questionnaire create(@RequestBody CreateQuestionnaireRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Questionnaire> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Questionnaire get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public Questionnaire addQuestion(@PathVariable String id, @RequestBody QuestionRequest request) {
        return service.addQuestion(id, request);
    }

    @PutMapping("/{id}/questions/{questionId}")
    public Questionnaire replaceQuestion(@PathVariable String id, @PathVariable String questionId,
                                         @RequestBody QuestionRequest request) {
        return service.replaceQuestion(id, questionId, request);
    }

    @PostMapping("/{id}/publish")
    public Questionnaire publish(@PathVariable String id) {
        return service.publish(id);
    }
}
