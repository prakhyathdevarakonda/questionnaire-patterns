# questionnaire-patterns

Lab for study map **week 2** (Area 1 Spring core, Area 6 Patterns), plus Area 4 immutability and Area 11 Mongo. Every class has a comment explaining the pattern and the **before (CES) → after** story. That story is your interview answer to "which patterns have you used and why".

## Run

```bash
docker compose up -d      # MongoDB
mvn spring-boot:run       # http://localhost:8080/api/questionnaires
mvn test
```

## Read in this order

1. `api/QuestionnaireController.java`: the HTTP entry point
2. `service/QuestionnaireService.java`: orchestration only (SRP, constructor injection)
3. `factory/QuestionnaireFactoryProvider.java`: Spring injects all factories as a `List` and indexes them by type; no switch (Open/Closed, fail-fast at startup)
4. `factory/QuestionnaireFactory.java` → `AbstractQuestionnaireFactory.java` → `StandardQuestionnaireFactory.java`: Abstract Factory + Template Method
5. `strategy/`: Strategy (`BranchingPolicy`, `PublishValidator`)
6. `domain/Questionnaire.java`: hand-written Builder, immutability, `@Version`
7. `domain/question/Question.java` + subclasses: `@SuperBuilder`, Liskov, Interface Segregation
8. `factory/QuestionFactory.java`: simple Factory
9. `test/.../QuestionnaireFactoriesTest.java`

For each file: explain it → delete the key part → rebuild it from memory.
