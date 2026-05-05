# AGENTS.md

## Project

Project name: Savings Calendar / Календарь накоплений.

Android application for daily savings tracking.

Core idea:
- Every day of the year has a number.
- Required daily contribution = dayOfYear * baseRate.
- User confirms that the contribution was made.
- App marks the day in calendar.
- Balance includes only confirmed days.
- Forecast includes already confirmed balance + future planned contributions from current date to the end of the year.
- Missed past days are not automatically included in future forecast.
- Monthly encouraging report is shown on the last day of each month.

Main stack:
- Kotlin
- Jetpack Compose
- Material 3
- Room
- DataStore
- AlarmManager / notification scaffolding
- Gradle Kotlin DSL

---

## Main Goal For Agents

Help develop the project with minimal token waste.

Prefer:
- small focused changes;
- targeted file reading;
- minimal diffs;
- deterministic logic;
- tests for business rules;
- clear commit messages.

Avoid:
- large rewrites;
- unnecessary architecture churn;
- dependency upgrades without request;
- reading the whole project when only 1-3 files are relevant;
- pasting full files into chat unless explicitly requested.

---

## Token Economy Rules

### 1. Read Less

Before opening files, use targeted search.

Preferred commands:

```bash
rg "SavingsCalculator|Forecast|Reminder|DataStore|Room|Calendar" .
find . -maxdepth 4 -type f | sort
```

Do not dump entire directories.

Do not read generated files, build outputs, or IDE files:

```text
.gradle/
build/
.idea/
*.iml
local.properties
```

### 2. Edit Small

Make the smallest change that solves the task.

Do not rewrite a file only to change formatting.

Do not rename classes, packages, modules, or files unless the task requires it.

### 3. Summarize Briefly

When reporting work, use this format:

```text
Done:
- Changed X to do Y.
- Added/updated tests for Z.
- Did not touch A/B/C.

Checks:
- ./gradlew testDebugUnitTest
```

No long explanations unless asked.

### 4. Ask Only When Blocked

Do not ask clarification questions for obvious implementation choices.

Make reasonable local decisions and document them briefly.

Ask only when:
- business rule is ambiguous;
- change may delete user data;
- change affects architecture significantly;
- dependency or platform choice is unclear.

### 5. Prefer Local Context

Use existing project style.

Before adding a new pattern, check whether the project already has:
- repository pattern;
- ViewModel pattern;
- UI state classes;
- theme colors;
- test naming style;
- package naming convention.

---

## Business Rules

### Daily Amount

```text
amount = dayOfYear * baseRate
```

Examples:
- day 1, baseRate 1 = 1
- day 53, baseRate 1 = 53
- day 53, baseRate 5 = 265

### Balance

Balance includes only confirmed entries.

```text
balance = sum(confirmedEntry.amount)
```

Do not calculate balance from theoretical calendar days.

### Forecast

Forecast to the end of the year:

```text
forecast = confirmedBalance + planned future contributions
```

Missed past days are not included.

Current day is included in future plan if it is not confirmed yet and the date has not passed.

### Base Rate Changes

Changing base rate affects only current and future calculations.

Already confirmed entries keep their saved amount and saved baseRate.

### Monthly Report

On the last day of each month, show an encouraging report:

```text
monthlyTotal = sum(confirmed entries in current month)
yearTotal = sum(confirmed entries in current year)
completedDaysInMonth = count(confirmed entries in current month)
```

The report must show facts at the time of report generation.

If today's contribution is not confirmed yet, it is not included.

---

## Architecture Rules

Keep business logic independent from Android framework when possible.

Preferred structure:

```text
data/
  db/
  model/
  repository/
  settings/

domain/
  SavingsCalculator.kt
  SavingsForecastService.kt
  MonthlyReportService.kt

notification/
  DailyReminderScheduler.kt
  MonthlyReportScheduler.kt
  ReminderReceiver.kt

ui/
  screen/
  component/
  theme/
```

Business calculations should live in `domain/`.

Android-specific APIs should not leak into pure calculator classes.

---

## Testing Rules

Always add or update tests when changing:

- daily amount calculation;
- balance calculation;
- forecast calculation;
- missed day behavior;
- base rate behavior;
- monthly report calculation;
- date handling;
- leap year behavior.

Preferred tests:
- fast JVM unit tests;
- deterministic dates;
- no reliance on current system date inside tests.

Use injected `LocalDate` or clock-like abstraction when needed.

Minimum calculator test cases:

```text
amountForDay(1, 1) = 1
amountForDay(53, 1) = 53
amountForDay(53, 5) = 265

fullYearPlan(365, 1) = 66795
fullYearPlan(366, 1) = 67161

confirmed balance ignores missed days
forecast ignores missed past days
confirmed entries keep old baseRate
monthly report sums only selected month
```

---

## Android Rules

### Compose

Prefer small composables.

Avoid putting business logic directly inside composables.

Use:
- state hoisting;
- ViewModel;
- immutable UI state;
- preview-friendly components where practical.

### Room

Do not change database schema without:
- updating entity;
- updating DAO;
- adding migration or clearly documenting destructive migration for MVP;
- updating tests where possible.

### DataStore

Use DataStore for simple settings:
- base rate;
- reminder enabled;
- reminder time;
- monthly report enabled;
- monthly report time;
- currency symbol.

### Notifications

Be careful with Android notification limitations.

Daily reminders:
- request notification permission on Android 13+;
- handle missing exact alarm permission gracefully;
- reschedule reminders when settings change;
- do not crash if scheduling fails.

Monthly reports:
- schedule for last day of month;
- reschedule next monthly report after firing;
- use actual confirmed entries at report time.

---

## Dependency Rules

Do not upgrade dependencies unless asked.

Do not add new libraries for simple tasks.

Before adding a dependency, explain briefly:
- why existing tools are insufficient;
- what the dependency solves;
- expected cost.

Prefer standard AndroidX libraries already used by the project.

---

## Commit Message Rules

Use detailed commits.

Format:

```text
type(scope): short summary

Files:
- path/File.kt: what changed
- path/Other.kt: what changed

Tests:
- Added/updated test X
- Ran command Y
```

Examples:

```text
feat(domain): add monthly savings report calculation

Files:
- domain/MonthlyReportService.kt: added monthly and yearly totals
- domain/MonthlyReportServiceTest.kt: covered month filtering and year totals

Tests:
- ./gradlew testDebugUnitTest
```

Allowed types:
- feat
- fix
- refactor
- test
- docs
- chore

---

## Work Modes

### Analysis Mode

Use when task is unclear or architectural.

Output:
- files likely affected;
- minimal implementation plan;
- risks;
- tests to run.

Keep it short.

### Implementation Mode

Use when task is clear.

Steps:
1. inspect only relevant files;
2. patch minimal code;
3. update/add tests;
4. run targeted checks;
5. summarize.

### Review Mode

Use when checking code.

Look for:
- broken business rules;
- wrong date logic;
- missed leap year cases;
- Room migration issues;
- notification permission issues;
- excessive recomposition in Compose;
- unnecessary dependencies;
- oversized files.

---

## Forbidden Without Explicit Request

Do not:
- rewrite the whole app;
- migrate to another architecture;
- introduce backend/cloud sync;
- add login;
- add bank integration;
- add analytics SDKs;
- add ads;
- change app name;
- replace Compose with XML;
- replace Room/DataStore without strong reason;
- reformat the entire project.

---

## Final Response Style For Codex

Be concise.

Good final response:

```text
Done:
- Added monthly report calculation.
- Added tests for month and year totals.
- Updated settings model with monthly report time.

Checks:
- ./gradlew testDebugUnitTest

Notes:
- Did not change notification scheduling yet.
```

Bad final response:
- long essays;
- full file dumps;
- repeating code already changed;
- explaining basic Kotlin syntax;
- listing every command output line.
