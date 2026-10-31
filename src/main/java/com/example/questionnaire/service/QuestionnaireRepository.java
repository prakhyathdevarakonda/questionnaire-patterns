package com.example.questionnaire.service;

import com.example.questionnaire.domain.Questionnaire;
import org.springframework.data.mongodb.repository.MongoRepository;

/*
 * PRINCIPLE: Dependency Inversion. The service depends on this interface; Spring Data
 * generates the Mongo implementation. CES called mongoManager.insertDocumentAsync(collection,
 * db, document) by hand from inside business methods.
 */
public interface QuestionnaireRepository extends MongoRepository<Questionnaire, String> {
}
