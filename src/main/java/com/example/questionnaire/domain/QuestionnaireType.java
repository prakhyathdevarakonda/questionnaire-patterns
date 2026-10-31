package com.example.questionnaire.domain;

/*
 * The "family" axis. Each value has exactly one QuestionnaireFactory (Abstract Factory).
 *
 * BEFORE (CES): screeningType was a free-form String compared with equalsIgnoreCase
 *   ("standard", "conversational", "workflow"), and pre-qualification was not a type at all;
 *   it lived on the side as a Kafka job event.
 * NOW: a closed enum. Typos fail at JSON parsing instead of silently falling into the
 *   "default" else-branch.
 */
public enum QuestionnaireType {
    STANDARD,
    CONVERSATIONAL,
    PRE_QUALIFICATION
}
