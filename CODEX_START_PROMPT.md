# CODEX_START_PROMPT.md

Use this prompt when starting Codex work on the project:

```text
Работай в режиме экономии токенов.

Сначала используй rg/find, затем читай только нужные файлы.
Делай минимальный diff.
Не печатай полные файлы без просьбы.
Не запускай дорогие проверки без необходимости.
После работы дай краткий отчет в формате:

Done:
- ...

Checks:
- ...

Notes:
- ...

Соблюдай правила из AGENTS.md и SKILL.md.
```
