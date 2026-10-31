package com.example.questionnaire.domain;

/*
 * The "product" axis. Which of these a questionnaire accepts is decided by its family's factory.
 *
 * BEFORE (CES): QuestionType existed, but every consumer compared getType() strings in long
 *   if/else chains (createQuestionDocumentFromRequest, updateQuestionDocument, ...).
 * NOW: the enum is switched on in exactly one place (QuestionFactory), with an exhaustive
 *   switch, so adding a value here is a compile error until it is handled.
 */
public enum QuestionType {
    VIDEO,
    AUDIO,
    TEXT,
    NUMBER,
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    DROPDOWN,
    YES_NO,
    KNOCKOUT
}
