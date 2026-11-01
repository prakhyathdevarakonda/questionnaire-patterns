#!/usr/bin/env bash
# Creates one questionnaire per family through the API, then shows the rules (factories/strategies) rejecting bad input.
# Needs: the app on :8083 (mvn spring-boot:run) and jq.  Usage: ./seed.sh
set -uo pipefail

API=${API:-http://localhost:8083/api/questionnaires}

call() { curl -s -X "$1" "$API$2" -H 'Content-Type: application/json' ${3:+-d "$3"}; }
new()  { call POST "" "{\"name\":\"$1\",\"type\":\"$2\",\"createdBy\":\"charan\"}" | jq -r .id; }
add()  { call POST "/$1/questions" "$2" | jq -r '.questions[-1].id'; }   # prints the new question's id

echo "== STANDARD: video screening (no branching)"
std=$(new "Backend Engineer - Video Screen" STANDARD)
add "$std" '{"type":"VIDEO","text":"Tell us about yourself","required":true,"maxRetakes":2,"durationLimitSec":90}' >/dev/null
add "$std" '{"type":"TEXT","text":"Describe a production incident you fixed","required":true,"maxCharacters":1000}' >/dev/null
add "$std" '{"type":"MULTIPLE_CHOICE","text":"Which have you used?","options":[{"label":"Spring Boot"},{"label":"Kafka"},{"label":"MongoDB"},{"label":"PostgreSQL"}]}' >/dev/null
call POST "/$std/publish" | jq -c '{id, name, type, draft, questions: (.questions | length)}'

echo "== CONVERSATIONAL: chatbot with forward-only branching"
conv=$(new "Graduate Hiring Chatbot" CONVERSATIONAL)
q1=$(add "$conv" '{"type":"YES_NO","text":"Are you willing to relocate?","required":true}')
q2=$(add "$conv" '{"type":"TEXT","text":"Which city do you prefer?"}')
q3=$(add "$conv" '{"type":"TEXT","text":"Anything else we should know?"}')
# Branch targets must already exist, so wire q1's options after q2/q3 are added: Yes -> q2, No -> q3.
call PUT "/$conv/questions/$q1" "{\"type\":\"YES_NO\",\"text\":\"Are you willing to relocate?\",\"required\":true,\"options\":[{\"label\":\"Yes\",\"nextQuestionId\":\"$q2\"},{\"label\":\"No\",\"nextQuestionId\":\"$q3\"}]}" >/dev/null
call POST "/$conv/publish" | jq -c '{id, name, type, draft, branches: [.questions[0].options[] | {label, nextQuestionId}]}'

echo "== PRE_QUALIFICATION: knockout screening"
pre=$(new "Delivery Partner Pre-check" PRE_QUALIFICATION)
add "$pre" '{"type":"KNOCKOUT","text":"Do you have a valid driving licence?","mustAnswerYes":true}' >/dev/null
add "$pre" '{"type":"NUMBER","text":"Years of driving experience","required":true,"min":0,"max":40}' >/dev/null
add "$pre" '{"type":"DROPDOWN","text":"Preferred shift","options":[{"label":"Morning"},{"label":"Evening"},{"label":"Night"}]}' >/dev/null
call POST "/$pre/publish" | jq -c '{id, name, type, draft, questions: (.questions | length)}'

echo "== Expected failures (each should be a 400/404 ProblemDetail)"
bad=$(new "No knockout yet" PRE_QUALIFICATION)
echo -n "VIDEO in PRE_QUALIFICATION (factory rule):   "; call POST "/$bad/questions" '{"type":"VIDEO","text":"Record a video"}' | jq -c '{status, detail}'
add "$bad" '{"type":"NUMBER","text":"Age","min":18}' >/dev/null   # non-empty, but still no KNOCKOUT
echo -n "publish without KNOCKOUT (strategy rule):    "; call POST "/$bad/publish" | jq -c '{status, detail}'
echo -n "edit a published questionnaire (immutable):  "; call POST "/$std/questions" '{"type":"TEXT","text":"Late question"}' | jq -c '{status, detail}'
echo -n "branch backwards (forward-only policy):      "
c2=$(new "Backward branch" CONVERSATIONAL)
a=$(add "$c2" '{"type":"TEXT","text":"First"}')
call POST "/$c2/questions" "{\"type\":\"SINGLE_CHOICE\",\"text\":\"Go back?\",\"options\":[{\"label\":\"Yes\",\"nextQuestionId\":\"$a\"},{\"label\":\"No\"}]}" | jq -c '{status, detail}'

echo "== All questionnaires"
call GET "" | jq -c '.[] | {name, type, draft, questions: (.questions | length)}'
