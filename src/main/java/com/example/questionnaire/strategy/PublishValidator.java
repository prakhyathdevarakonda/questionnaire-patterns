package com.example.questionnaire.strategy;

import com.example.questionnaire.domain.Questionnaire;

/*
 * PATTERN: Strategy. "When is a questionnaire ready to publish?" differs per family.
 *
 * BEFORE (CES): autoPublishQuestionnaireAfterUpdatedBy had
 *     if (SCREENING_TYPE_WORKFLOW.equalsIgnoreCase(type) && !isValidWorkFlowQuestionnaireForPublish(...))
 *   Every new family meant another `&& type == X` clause in the publish flow.
 * NOW: publish() runs factory.publishValidator().validate(q) and stays the same forever.
 */
public interface PublishValidator {

    void validate(Questionnaire questionnaire);
}
