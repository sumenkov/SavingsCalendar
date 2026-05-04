# Календарь накоплений

Android-приложение для ежедневной отметки накоплений: каждый день пользователь откладывает сумму, равную номеру дня в году × базовая ставка, подтверждает взнос, видит календарь, баланс, прогноз до конца года и ежемесячные ободряющие отчёты.

## Что уже заложено в проект

- Kotlin + Jetpack Compose + Material 3.
- Локальное хранение отметок через Room.
- Настройки через DataStore Preferences.
- Бизнес-логика расчёта накоплений вынесена в `domain`.
- Ежедневные уведомления и ежемесячные отчёты вынесены в `notification`.
- Векторная временная иконка по концепции: монета `₽` + календарь + галочка.
- Инструкции для Codex: `AGENTS.md` и `SKILL.md`.

## Основная формула

```text
amount = dayOfYear * baseRate
```

По умолчанию:

```text
baseRate = 1 ₽
```

Баланс считается только по подтверждённым дням. Пропущенные прошлые дни не попадают в прогноз автоматически.

## Структура

```text
app/src/main/java/ru/sumenkov/savingscalendar/
  data/
    db/
    repository/
    settings/
  domain/
  notification/
  ui/
    screen/
    theme/
  MainActivity.kt
  SavingsCalendarApplication.kt
```

## Как открыть проект

1. Открой Android Studio.
2. Выбери **Open**.
3. Укажи папку проекта `savings-calendar-android`.
4. Дождись Gradle Sync.
5. Запусти конфигурацию `app`.

## Gradle wrapper

В архиве намеренно не лежит `gradle-wrapper.jar`, чтобы не тащить бинарник неизвестного происхождения. Android Studio обычно сама предложит использовать установленный Gradle или скачать wrapper при синхронизации.

Если хочешь создать wrapper вручную:

```bash
cd savings-calendar-android
gradle wrapper --gradle-version 8.10.2
./gradlew :app:assembleDebug
```

Если `gradle` не установлен, проще открыть проект в Android Studio и дать IDE выполнить синхронизацию.

## Иконка

Сейчас в проекте лежит временная векторная иконка:

```text
app/src/main/res/drawable/ic_launcher_foreground.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

Если у тебя есть финальная иконка из соседнего чата в PNG/SVG:

- для SVG: замени `ic_launcher_foreground.xml` после конвертации в Vector Drawable;
- для PNG: сгенерируй adaptive icon через Android Studio: **New > Image Asset**;
- имя приложения и package менять не нужно.

## Текущие цвета

```text
Primary:      #2E7D5A
PrimaryLight: #DDF3E8
Accent:       #E9B949
Background:   #F8FAF7
TextPrimary:  #1F2933
TextSecondary:#5B6870
```

## Что проверить первым делом

- Расчёт суммы дня.
- Сохранение отметки сегодняшнего дня.
- Изменение базовой ставки в настройках.
- Баланс по подтверждённым дням.
- Прогноз до конца года без догоняния пропущенных дней.
- Ежемесячный отчёт за текущий месяц и год.
- Разрешение уведомлений на Android 13+.
- Поведение exact alarm на Android 12+ / Android 14+.

## Ближайшие задачи

1. Довести календарь до полноценной месячной сетки с переключением месяцев.
2. Добавить экран деталей дня.
3. Сделать отдельный экран месячного отчёта.
4. Добавить UI для разрешений уведомлений и exact alarm.
5. Покрыть `SavingsCalculator` unit-тестами.
6. Добавить инструментальную проверку сохранения отметок в Room.
