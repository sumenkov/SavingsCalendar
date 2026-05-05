# SKILL.md

## Skill: Token-Efficient Android Development

This skill describes how to work on this Android project while spending fewer tokens.

Use it for:
- Kotlin changes;
- Jetpack Compose UI;
- Room/DataStore updates;
- notification scheduling;
- date and savings calculations;
- tests;
- documentation updates.

---

## Golden Rule

Do the smallest correct change.

Every task should follow this loop:

```text
search -> read relevant files -> patch -> test -> summarize
```

Never start by reading the whole project.

---

## Project Mental Model

This app tracks daily savings.

The user saves:

```text
dayOfYear * baseRate
```

The user must confirm each day manually.

Only confirmed days count as saved money.

Missed days stay missed unless manually confirmed.

Forecast uses:
- confirmed balance;
- future planned days;
- no automatic catch-up for missed past days.

Monthly reports show:
- saved this month;
- saved since start of year;
- count of confirmed days in month.

---

## Fast File Discovery

Use these commands before reading files:

```bash
find . -maxdepth 4 -type f | sort
rg "class Savings|fun amount|forecast|Monthly|Reminder|DataStore|Room|@Entity|@Dao" .
```

For UI tasks:

```bash
rg "Composable|Home|Calendar|History|Settings|Theme" app/src/main
```

For tests:

```bash
find . -path "*test*" -type f | sort
rg "SavingsCalculator|Forecast|MonthlyReport" app/src/test
```

Do not inspect:
- build outputs;
- Gradle caches;
- IDE files;
- generated files.

---

## Reading Strategy

Read only what is needed.

Recommended maximum initial read:
- 1-3 files for a simple bug;
- 3-6 files for a feature;
- more only after search proves it is necessary.

When a file is long:
- search inside it first;
- read only relevant sections;
- avoid full dumps.

---

## Patch Strategy

Prefer surgical patches.

Good:
- add one function;
- add one test file;
- update one UI state;
- update one DAO query.

Bad:
- reorganize packages while fixing a bug;
- rename unrelated classes;
- format entire files;
- introduce new abstractions too early.

---

## Testing Strategy

Run the smallest useful test first.

For domain logic:

```bash
./gradlew testDebugUnitTest
```

For a specific test class, use a targeted Gradle test filter when available:

```bash
./gradlew testDebugUnitTest --tests "*SavingsCalculatorTest"
```

For build check:

```bash
./gradlew assembleDebug
```

Do not run expensive checks repeatedly unless the task requires it.

Always report what was actually run.

If checks cannot run, say why briefly.

---

## Date Logic Rules

Use `java.time.LocalDate`.

Do not use current system date directly inside business logic.

Prefer passing date as a parameter.

Important cases:
- January 1;
- December 31;
- leap year February 29;
- last day of each month;
- changing base rate mid-year;
- missed days before today;
- current day not yet confirmed.

---

## Savings Calculation Rules

### Daily amount

```kotlin
amount = dayOfYear * baseRate
```

Use integer-safe types.

Prefer `Long` for money in minor units or whole rubles.

Do not use floating point for money.

### Full year plan

```text
daysInYear * (daysInYear + 1) / 2 * baseRate
```

Expected:
- 365 days, rate 1 = 66795
- 366 days, rate 1 = 67161

### Confirmed balance

Sum stored confirmed entries.

Do not infer balance from calendar position.

### Forecast

Forecast must not include missed past days.

---

## Monthly Report Rules

Monthly report data:

```text
monthTotal = sum confirmed entries where year/month match
yearTotal = sum confirmed entries where year matches
completedDaysInMonth = count confirmed entries where year/month match
daysInMonth = date.lengthOfMonth()
```

Report generation date matters.

The last day of the month is:

```kotlin
date.withDayOfMonth(date.lengthOfMonth())
```

Next report should be scheduled for the next month's last day.

---

## Compose Rules

Keep composables small.

Do not put database calls inside composables.

Do not put savings formulas inside composables.

Use UI state classes:

```kotlin
data class HomeUiState(...)
data class SettingsUiState(...)
data class CalendarUiState(...)
```

Prefer simple stateless components:

```kotlin
@Composable
fun BalanceCard(...)
```

State should come from ViewModel.

---

## Room Rules

When changing an entity:
1. update entity;
2. update DAO;
3. update database version;
4. add migration or document MVP destructive migration;
5. update repository;
6. update tests.

For confirmed savings entries, store:
- date;
- year;
- dayOfYear;
- baseRate;
- amount;
- confirmedAt.

Reason:
- old confirmed amounts must not change when base rate changes.

---

## DataStore Rules

Use DataStore for simple settings.

Settings:
- baseRate;
- currencySymbol;
- dailyReminderEnabled;
- dailyReminderHour;
- dailyReminderMinute;
- monthlyReportEnabled;
- monthlyReportHour;
- monthlyReportMinute;
- allowPastDayConfirmation.

Do not store history in DataStore.

---

## Notification Rules

Daily reminders:
- show today's required amount;
- include action "Отложил" when implemented;
- request notification permission on Android 13+;
- gracefully handle exact alarm limitations.

Monthly report:
- fire on last day of month;
- show month total and year total;
- open report/details screen when tapped;
- do not include unconfirmed days.

Avoid fragile assumptions:
- exact alarms may be unavailable;
- device may reboot;
- app may be battery restricted.

---

## Dependency Rules

Default answer to new dependency: no.

Add dependency only when:
- it removes significant complexity;
- it is standard for Android;
- the task cannot reasonably be done with current stack.

Never upgrade Kotlin, AGP, Compose, Room, or DataStore unless requested.

---

## Documentation Rules

Update docs only when behavior changes.

Keep docs short and practical.

Prefer:
- commands;
- paths;
- business rules;
- examples.

Avoid:
- long theory;
- duplicate explanations;
- stale architecture diagrams.

---

## Final Answer Format

Use this format after work:

```text
Done:
- ...

Checks:
- ...

Notes:
- ...
```

Keep it compact.

Mention only important files.

Do not paste entire files unless requested.

---

## Common Tasks

### Add a new business rule

1. Find domain service/calculator.
2. Add or update function.
3. Add unit tests.
4. Update ViewModel/repository only if needed.
5. Update docs briefly.

### Add a new setting

1. Update settings model.
2. Update DataStore keys.
3. Update Settings UI.
4. Apply setting where used.
5. Add tests if logic changes.

### Add a new screen card

1. Add small composable.
2. Wire it to existing UI state.
3. Avoid direct repository access from composable.
4. Preview if project already uses previews.

### Fix forecast bug

1. Read calculator/forecast service.
2. Add failing test.
3. Fix formula.
4. Verify missed days are ignored.
5. Verify current day behavior.

### Fix notification bug

1. Read scheduler and receiver only.
2. Check permissions and intent actions.
3. Avoid changing business logic.
4. Add logs only if project already uses logging.
5. Test manually or document why not possible.

---

## Things Not To Do

Do not:
- create a backend;
- add authorization;
- integrate banks;
- add cloud sync;
- redesign the entire UI;
- rewrite state management;
- move all code between packages;
- add multiplatform support;
- convert to another database;
- introduce coroutines/flows where simple existing code works, unless project already uses them.

---

## Compact Commit Template

```text
feat(scope): summary

Files:
- path: change
- path: change

Tests:
- command or "not run: reason"
```

Example:

```text
fix(domain): exclude missed days from forecast

Files:
- app/src/main/java/.../SavingsForecastService.kt: fixed remaining plan calculation
- app/src/test/java/.../SavingsForecastServiceTest.kt: added missed day cases

Tests:
- ./gradlew testDebugUnitTest --tests "*SavingsForecastServiceTest"
```
