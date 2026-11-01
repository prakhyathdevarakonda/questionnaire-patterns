# questionnaire-patterns

Lab for study map **Week 2** (Spring core, Design patterns), plus immutability (Week 1) and MongoDB (Week 4). Every class has a comment explaining the pattern and the **before (CES) → after** story. That story is your interview answer to "which patterns have you used and why".

## Run locally

One-time install: Docker Desktop, Maven, Java 17+ (`brew install --cask docker`, `brew install maven openjdk@21`).

```bash
docker compose up -d                   # MongoDB 7 on localhost:27017 (-d = runs in the background)
docker compose --profile tools up -d   # optional: mongo-express UI on http://localhost:8091
mvn spring-boot:run                    # app on http://localhost:8083 (8080 is left for wallet-service)
./seed.sh                              # sample data, in a second terminal
mvn test                               # no Docker needed
```

Stop: `docker compose --profile tools down` (keeps data) · add `-v` to wipe it. Each `./seed.sh` run adds a fresh set, so use `down -v` to start clean.

**No Maven installed?** Run the app inside the official Maven image instead (dependencies are cached in a Docker volume):

```bash
docker run -d --name questionnaire-app -p 8083:8083 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/questionnaire_patterns \
  -v "$PWD":/app -v questionnaire-m2:/root/.m2 -w /app \
  maven:3.9.9-eclipse-temurin-21 mvn -q spring-boot:run
docker logs -f questionnaire-app      # wait for "Started QuestionnairePatternsApplication"
docker rm -f questionnaire-app        # stop it
```

## What `./seed.sh` creates

| Questionnaire | Type | Shows |
| --- | --- | --- |
| Backend Engineer - Video Screen | STANDARD | VIDEO / TEXT / MULTIPLE_CHOICE, no branching |
| Graduate Hiring Chatbot | CONVERSATIONAL | YES_NO branching: Yes → city question, No → last question |
| Delivery Partner Pre-check | PRE_QUALIFICATION | KNOCKOUT + NUMBER + DROPDOWN; publish needs a knockout |

Then it triggers four **expected failures**, each one a pattern doing its job: a factory rejects a disallowed question type, a publish strategy rejects a missing knockout, an immutable published questionnaire rejects an edit, and the forward-only branching policy rejects a backward jump.

## APIs (base `http://localhost:8083/api/questionnaires`)

| Method | Path | Body |
| --- | --- | --- |
| POST | `/` | `{"name":"...","type":"STANDARD\|CONVERSATIONAL\|PRE_QUALIFICATION","createdBy":"charan"}` |
| GET | `/` and `/{id}` | |
| POST | `/{id}/questions` | `{"type":"TEXT","text":"...","required":true,"maxCharacters":500}` |
| PUT | `/{id}/questions/{questionId}` | same as above (replaces the question) |
| POST | `/{id}/publish` | (none). After this the questionnaire is read-only |

Question fields by type: `VIDEO/AUDIO` → `maxRetakes`, `durationLimitSec` · `TEXT` → `maxCharacters` · `NUMBER` → `min`, `max` · `SINGLE_CHOICE/MULTIPLE_CHOICE/DROPDOWN/YES_NO` → `options: [{"label","nextQuestionId"}]` · `KNOCKOUT` → `mustAnswerYes`.

Allowed types per family: STANDARD = VIDEO, AUDIO, TEXT, MULTIPLE_CHOICE · CONVERSATIONAL = TEXT, SINGLE_CHOICE, MULTIPLE_CHOICE, YES_NO · PRE_QUALIFICATION = YES_NO, KNOCKOUT, NUMBER, DROPDOWN.

Errors come back as `ProblemDetail` JSON: 400 for rule violations, 404 if not found, 409 on a concurrent edit (`@Version`).

## See the data

**Terminal (mongosh):**

```bash
docker exec -it questionnaire-mongo mongosh questionnaire_patterns
```

```js
db.questionnaires.countDocuments()
db.questionnaires.find({}, {name: 1, type: 1, draft: 1, version: 1})      // version = optimistic lock counter
db.questionnaires.findOne({type: "CONVERSATIONAL"})                       // questions are EMBEDDED in one document
db.questionnaires.find({}, {"questions._class": 1})                       // how Spring stores the subclass of each question
db.questionnaires.find({type: "PRE_QUALIFICATION"}).explain("executionStats")  // COLLSCAN: no index yet (Week 4 lab)
```

**UI:** http://localhost:8091 (mongo-express) → database `questionnaire_patterns` → `questionnaires`.

## Read the code in this order

1. `api/QuestionnaireController.java`: the HTTP entry point
2. `service/QuestionnaireService.java`: orchestration only (SRP, constructor injection)
3. `factory/QuestionnaireFactoryProvider.java`: Spring injects all factories as a `List` and indexes them by type; no switch (Open/Closed, fail-fast at startup)
4. `factory/QuestionnaireFactory.java` → `AbstractQuestionnaireFactory.java` → `StandardQuestionnaireFactory.java`: Abstract Factory + Template Method
5. `strategy/`: Strategy (`BranchingPolicy`, `PublishValidator`)
6. `domain/Questionnaire.java`: hand-written Builder, immutability, `@Version`
7. `domain/question/Question.java` + subclasses: `@SuperBuilder`, Liskov, Interface Segregation
8. `factory/QuestionFactory.java`: simple Factory
9. `test/.../QuestionnaireFactoriesTest.java`

For each file: explain it → delete the key part → rebuild it from memory. Run `./seed.sh` after each change to check nothing broke.
