package com.example.questionnaire.domain;

import com.example.questionnaire.domain.question.Question;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/*
 * PATTERN: Builder (hand-written, so you can see every moving part).
 * PRINCIPLE: Immutability. No setters; every change returns a new Questionnaire.
 *
 * BEFORE (CES): VideoQuestionnaireService.createQuestionnaire did `new VideoQuestionnaireModel()`
 *   followed by ~15 setters (id, version=1, draft=true, createdBy, default settings, empty
 *   intro/outro, locales...). Nothing stopped a caller from forgetting one, and the object was
 *   mutable everywhere it travelled afterwards.
 * NOW: Questionnaire.builder()...build().
 *   - Defaults (draft=true, default retakes/duration, createdAt) live in ONE place: the Builder.
 *   - build() validates, so an invalid Questionnaire can never exist (fail fast).
 *   - id and version are private on the Builder: only Mongo (via toBuilder) may carry them.
 * WHY a builder and not a constructor: 9 fields, several optional, several with defaults.
 *   A 9-argument constructor is unreadable at the call site and easy to get in the wrong order.
 *
 * Questions are embedded in the questionnaire document (one aggregate = one document), which is
 * why Mongo fits well: the different question subclasses are stored as-is with a `_class` hint.
 */
@Document("questionnaires")
public final class Questionnaire {

    @Id
    private final String id;
    private final String name;
    private final QuestionnaireType type;
    private final boolean draft;
    private final String createdBy;
    private final Instant createdAt;
    private final int defaultMaxRetakes;
    private final int defaultDurationLimitSec;
    private final List<Question> questions;

    /*
     * Optimistic locking: two concurrent "add question" requests both read version N; the
     * second save fails instead of silently overwriting the first one's question (lost update).
     */
    @Version
    private final Long version;

    @PersistenceCreator
    Questionnaire(String id, String name, QuestionnaireType type, boolean draft, String createdBy,
                  Instant createdAt, int defaultMaxRetakes, int defaultDurationLimitSec,
                  List<Question> questions, Long version) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.draft = draft;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.defaultMaxRetakes = defaultMaxRetakes;
        this.defaultDurationLimitSec = defaultDurationLimitSec;
        this.questions = questions == null ? List.of() : List.copyOf(questions);
        this.version = version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .version(version)
                .name(name)
                .type(type)
                .draft(draft)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .defaultMaxRetakes(defaultMaxRetakes)
                .defaultDurationLimitSec(defaultDurationLimitSec)
                .questions(questions);
    }

    public Questionnaire withQuestion(Question question) {
        requireDraft();
        if (containsQuestion(question.getId())) {
            throw new InvalidQuestionnaireException("question " + question.getId() + " already exists");
        }
        return toBuilder().addQuestion(question).build();
    }

    public Questionnaire withReplacedQuestion(Question question) {
        requireDraft();
        int index = indexOf(question.getId());
        if (index < 0) {
            throw new NotFoundException("question " + question.getId() + " not found");
        }
        List<Question> updated = new ArrayList<>(questions);
        updated.set(index, question);
        return toBuilder().questions(updated).build();
    }

    public Questionnaire published() {
        requireDraft();
        return toBuilder().draft(false).build();
    }

    public boolean containsQuestion(String questionId) {
        return indexOf(questionId) >= 0;
    }

    public int indexOf(String questionId) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(questionId)) {
                return i;
            }
        }
        return -1;
    }

    private void requireDraft() {
        if (!draft) {
            throw new InvalidQuestionnaireException("questionnaire " + id + " is published and can no longer be changed");
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public QuestionnaireType getType() { return type; }
    public boolean isDraft() { return draft; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public int getDefaultMaxRetakes() { return defaultMaxRetakes; }
    public int getDefaultDurationLimitSec() { return defaultDurationLimitSec; }
    public List<Question> getQuestions() { return questions; }
    public Long getVersion() { return version; }

    public static final class Builder {

        private String id;
        private Long version;
        private String name;
        private QuestionnaireType type;
        private boolean draft = true;
        private String createdBy;
        private Instant createdAt;
        private int defaultMaxRetakes = 1;
        private int defaultDurationLimitSec = 120;
        private final List<Question> questions = new ArrayList<>();

        private Builder() {
        }

        private Builder id(String id) {
            this.id = id;
            return this;
        }

        private Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(QuestionnaireType type) {
            this.type = type;
            return this;
        }

        public Builder draft(boolean draft) {
            this.draft = draft;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder defaultMaxRetakes(int defaultMaxRetakes) {
            this.defaultMaxRetakes = defaultMaxRetakes;
            return this;
        }

        public Builder defaultDurationLimitSec(int defaultDurationLimitSec) {
            this.defaultDurationLimitSec = defaultDurationLimitSec;
            return this;
        }

        public Builder questions(List<Question> questions) {
            this.questions.clear();
            this.questions.addAll(questions);
            return this;
        }

        public Builder addQuestion(Question question) {
            this.questions.add(question);
            return this;
        }

        public Questionnaire build() {
            if (name == null || name.isBlank()) {
                throw new InvalidQuestionnaireException("questionnaire name is required");
            }
            if (type == null) {
                throw new InvalidQuestionnaireException("questionnaire type is required");
            }
            if (createdBy == null || createdBy.isBlank()) {
                throw new InvalidQuestionnaireException("createdBy is required");
            }
            if (defaultMaxRetakes < 0) {
                throw new InvalidQuestionnaireException("defaultMaxRetakes must be >= 0");
            }
            if (defaultDurationLimitSec <= 0) {
                throw new InvalidQuestionnaireException("defaultDurationLimitSec must be > 0");
            }
            return new Questionnaire(id, name.trim(), type, draft, createdBy,
                    createdAt == null ? Instant.now() : createdAt,
                    defaultMaxRetakes, defaultDurationLimitSec, questions, version);
        }
    }
}
