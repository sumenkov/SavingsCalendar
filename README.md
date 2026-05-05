# Календарь накоплений

Android-приложение для ежедневной отметки накоплений: пользователь откладывает сумму по выбранному режиму, подтверждает взнос, видит календарь, баланс, прогноз до конца периода накоплений и ежемесячные ободряющие отчёты.

## Что уже заложено в проект

- Kotlin + Jetpack Compose + Material 3.
- Локальное хранение отметок через Room.
- Настройки через DataStore Preferences.
- Бизнес-логика расчёта накоплений вынесена в `domain`.
- Ежедневные уведомления и ежемесячные отчёты вынесены в `notification`.
- Векторная временная иконка по концепции: монета `₽` + календарь + галочка.
- Инструкции для Codex: `AGENTS.md` и `SKILL.md`.

## Основная формула

По умолчанию включён режим роста суммы. Номер дня считается от начала выбранного периода накоплений:

```text
amount = dayNumberInPeriod * baseRate
```

Также доступен режим ровных сумм:

```text
amount = baseRate
```

По умолчанию:

```text
baseRate = 1 ₽
period = 1 января - 31 декабря
```

Баланс считается только по подтверждённым дням. Пользователь может вручную отметить прошлый или будущий день внутри периода накоплений. Пропущенные прошлые дни не попадают в прогноз автоматически.

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

В проекте хранится Gradle wrapper, включая `gradle/wrapper/gradle-wrapper.jar`. Текущая версия wrapper настроена в `gradle/wrapper/gradle-wrapper.properties`; сейчас это Gradle `9.3.0`.

Для сборки из терминала:

```bash
cd savings-calendar-android
./gradlew :app:assembleDebug
```

Если wrapper нужно пересоздать вручную, используй ту же версию Gradle, что указана в `distributionUrl`.

## Иконка и логотип

В проекте используются PNG-ассеты:

```text
app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png
app/src/main/res/drawable-nodpi/app_logo.png
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

Launcher icon подключён через adaptive icon XML, логотип показывается на главном экране.

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
- Сохранение отметки будущего дня внутри периода накоплений.
- Изменение базовой ставки в настройках.
- Переключение между ростом суммы и ровными суммами.
- Изменение начала и конца периода накоплений.
- Баланс по подтверждённым дням.
- Прогноз до конца периода без догоняния пропущенных дней.
- Ежемесячный отчёт за текущий месяц и год.
- Разрешение уведомлений на Android 13+.
- Поведение exact alarm на Android 12+ / Android 14+.

## Room schema

Room schema export включён намеренно. JSON-схемы лежат в `app/schemas/` и должны попадать в коммит вместе с изменениями структуры базы, чтобы миграции можно было проверять воспроизводимо.

## Ближайшие задачи

1. Сделать отдельный экран месячного отчёта.
2. Прогнать инструментальные Room-тесты на устройстве или эмуляторе.
3. Проверить UX разрешений уведомлений на Android 13+ и exact alarm на Android 12+/14+.
