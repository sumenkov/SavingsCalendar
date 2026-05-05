package ru.sumenkov.savingscalendar.ui

import ru.sumenkov.savingscalendar.data.settings.AppLanguage
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class SavingsStrings private constructor(
    val language: AppLanguage,
    val locale: Locale,
    val appName: String,
    val appLogoContentDescription: String,
    val homeTab: String,
    val calendarTab: String,
    val historyTab: String,
    val settingsTab: String,
    val todayOutsidePeriod: String,
    val fixedAmountMode: String,
    val growthAmountMode: String,
    val todayContributionConfirmed: String,
    val makeContribution: String,
    val currentBalance: String,
    val planToPeriodEnd: String,
    val monthlyReportPrefix: String,
    val monthTotal: String,
    val sinceYearStart: String,
    val completedDays: String,
    val monthProgress: String,
    val previous: String,
    val next: String,
    val contributionDone: String,
    val missed: String,
    val today: String,
    val amount: String,
    val rate: String,
    val outsideSavingsPeriod: String,
    val statusConfirmed: String,
    val statusUnmarked: String,
    val cancelContribution: String,
    val markDay: String,
    val cannotMarkDay: String,
    val back: String,
    val historyEmpty: String,
    val settingsTitle: String,
    val helpTitle: String,
    val helpSubtitle: String,
    val open: String,
    val helpDialogTitle: String,
    val understood: String,
    val languageTitle: String,
    val languageSubtitle: String,
    val systemLanguage: String,
    val englishLanguage: String,
    val russianLanguage: String,
    val fixedAmountTitle: String,
    val baseRateTitle: String,
    val dailyAmountLabel: String,
    val rateLabel: String,
    val currencySymbol: String,
    val settingsAffectFuture: String,
    val fixedAmounts: String,
    val fixedAmountsSubtitle: String,
    val growthAmountsSubtitle: String,
    val accumulationPeriod: String,
    val start: String,
    val end: String,
    val select: String,
    val change: String,
    val notificationPermissions: String,
    val notificationsDisabled: String,
    val allowNotifications: String,
    val exactAlarmsDisabled: String,
    val openAlarmSettings: String,
    val dailyReminders: String,
    val dailyReminderTime: String,
    val monthlyReports: String,
    val monthlyReportTime: String,
    val allowPastDays: String,
    val allowPastDaysSubtitle: String,
    private val cancelContributionQuestionText: String,
    private val cancelContributionForText: String,
    private val historyTitleText: String,
    private val dayPeriodText: String,
    private val dayYearText: String,
    private val periodText: String,
    private val daysOfText: String,
    private val dailyNotificationTitleText: String,
    private val dailyNotificationText: String,
    private val monthlyNotificationTitleText: String,
    private val monthlyNotificationShortText: String,
    private val monthlyNotificationLongText: String,
    val weekDays: List<String>,
    val helpSections: List<Pair<String, String>>
) {
    fun dayPeriod(number: Int): String = dayPeriodText.format(number)

    fun dayYear(number: Int): String = dayYearText.format(number)

    fun period(start: String, end: String): String = periodText.format(start, end)

    fun daysOf(completed: Int, total: Int): String = daysOfText.format(completed, total)

    fun historyTitle(total: Long, currency: String): String = historyTitleText.format(total, currency)

    fun cancelContributionQuestion(): String = cancelContributionQuestionText

    fun cancelContributionFor(date: String, amount: Long, currency: String): String {
        return cancelContributionForText.format(date, amount, currency)
    }

    fun dailyNotificationTitle(dayNumber: Int): String = dailyNotificationTitleText.format(dayNumber)

    fun dailyNotification(amount: Long, currency: String): String = dailyNotificationText.format(amount, currency)

    fun monthlyNotificationTitle(monthName: String): String = monthlyNotificationTitleText.format(monthName)

    fun monthlyNotificationShort(monthTotal: Long, yearTotal: Long, currency: String): String {
        return monthlyNotificationShortText.format(monthTotal, currency, yearTotal, currency)
    }

    fun monthlyNotificationLong(monthTotal: Long, yearTotal: Long, completedDays: Int, daysInMonth: Int, currency: String): String {
        return monthlyNotificationLongText.format(monthTotal, currency, yearTotal, currency, completedDays, daysInMonth)
    }

    fun fullDate(date: LocalDate): String {
        val pattern = if (language == AppLanguage.RU) "d MMMM yyyy" else "MMMM d, yyyy"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun monthDay(date: LocalDate): String {
        val pattern = if (language == AppLanguage.RU) "d MMMM" else "MMMM d"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun periodDate(date: LocalDate): String {
        val pattern = if (language == AppLanguage.RU) "d MMMM" else "MMM d"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun monthName(yearMonth: YearMonth): String {
        return yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun monthTitle(yearMonth: YearMonth): String = "${monthName(yearMonth)} ${yearMonth.year}"

    fun languageOption(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SYSTEM -> systemLanguage
            AppLanguage.EN -> englishLanguage
            AppLanguage.RU -> russianLanguage
        }
    }

    companion object {
        fun from(languageSetting: AppLanguage, systemLocale: Locale = Locale.getDefault()): SavingsStrings {
            return when (languageSetting.resolve(systemLocale)) {
                AppLanguage.EN -> english(languageSetting.locale(systemLocale))
                AppLanguage.RU -> russian(languageSetting.locale(systemLocale))
                AppLanguage.SYSTEM -> english(systemLocale)
            }
        }

        private fun russian(locale: Locale): SavingsStrings {
            return SavingsStrings(
                language = AppLanguage.RU,
                locale = locale,
                appName = "Календарь накоплений",
                appLogoContentDescription = "Логотип приложения",
                homeTab = "Сегодня",
                calendarTab = "Календарь",
                historyTab = "История",
                settingsTab = "Настройки",
                todayOutsidePeriod = "Сегодня вне периода",
                fixedAmountMode = "Режим: ровная сумма",
                growthAmountMode = "Режим: рост по дню периода",
                todayContributionConfirmed = "Взнос за сегодня внесён",
                makeContribution = "Внести взнос",
                currentBalance = "Текущий баланс",
                planToPeriodEnd = "План до конца периода",
                monthlyReportPrefix = "Итоги месяца",
                monthTotal = "За месяц",
                sinceYearStart = "С начала года",
                completedDays = "Отмечено дней",
                monthProgress = "Прогресс месяца",
                previous = "Назад",
                next = "Вперёд",
                contributionDone = "Взнос внесён",
                missed = "Пропущено",
                today = "Сегодня",
                amount = "Сумма",
                rate = "Ставка",
                outsideSavingsPeriod = "Вне периода накоплений",
                statusConfirmed = "Статус: взнос внесён",
                statusUnmarked = "Статус: не отмечено",
                cancelContribution = "Отменить взнос",
                markDay = "Отметить день",
                cannotMarkDay = "Нельзя отметить этот день",
                back = "Назад",
                historyEmpty = "Пока нет подтверждённых взносов. Первая монетка ещё ждёт своего часа.",
                settingsTitle = "Настройки",
                helpTitle = "Помощь",
                helpSubtitle = "Инструкция по расчёту, отметкам, балансу и прогнозу.",
                open = "Открыть",
                helpDialogTitle = "Помощь и инструкция",
                understood = "Понятно",
                languageTitle = "Язык",
                languageSubtitle = "По умолчанию используется язык системы.",
                systemLanguage = "Системный",
                englishLanguage = "English",
                russianLanguage = "Русский",
                fixedAmountTitle = "Ровная сумма",
                baseRateTitle = "Базовая ставка",
                dailyAmountLabel = "Сумма в день",
                rateLabel = "Ставка",
                currencySymbol = "Символ валюты",
                settingsAffectFuture = "Изменение влияет только на будущие отметки.",
                fixedAmounts = "Ровные суммы",
                fixedAmountsSubtitle = "Каждый день откладывается одна и та же сумма.",
                growthAmountsSubtitle = "Сумма растёт по формуле: день периода × базовая ставка.",
                accumulationPeriod = "Период накоплений",
                start = "Начало",
                end = "Конец",
                select = "Выбрать",
                change = "Изменить",
                notificationPermissions = "Разрешения уведомлений",
                notificationsDisabled = "Уведомления выключены. Без разрешения напоминания и месячные отчёты не будут показаны.",
                allowNotifications = "Разрешить уведомления",
                exactAlarmsDisabled = "Точные будильники выключены. Напоминания могут приходить не строго в заданное время.",
                openAlarmSettings = "Открыть настройки будильников",
                dailyReminders = "Ежедневные напоминания",
                dailyReminderTime = "Время ежедневного напоминания",
                monthlyReports = "Месячные отчёты",
                monthlyReportTime = "Время месячного отчёта",
                allowPastDays = "Разрешить отметки прошлых дней",
                allowPastDaysSubtitle = "Пропущенные дни можно отметить вручную, но прогноз их не догоняет автоматически.",
                cancelContributionQuestionText = "Отменить взнос?",
                cancelContributionForText = "Отменить взнос за %s на сумму %d %s?",
                historyTitleText = "История: %d %s",
                dayPeriodText = "День периода №%d",
                dayYearText = "День года №%d",
                periodText = "Период: %s - %s",
                daysOfText = "%d из %d",
                dailyNotificationTitleText = "Сегодня день периода №%d",
                dailyNotificationText = "Внесите %d %s в накопления.",
                monthlyNotificationTitleText = "Итоги месяца: %s",
                monthlyNotificationShortText = "За месяц: %d %s, всего за год: %d %s.",
                monthlyNotificationLongText = "За месяц внесено %d %s. Всего с начала года: %d %s. Отмечено дней: %d из %d.",
                weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"),
                helpSections = listOf(
                    "Что это за приложение" to "Календарь накоплений помогает откладывать деньги по дням. Вы выбираете период, сумму и режим расчёта, а приложение показывает план, баланс и отмеченные взносы.",
                    "Как начать" to "Откройте настройки, задайте базовую ставку или ровную сумму, выберите первый и последний день накоплений. По умолчанию период длится с 1 января по 31 декабря.",
                    "Как считается сумма" to "В режиме роста первый день выбранного периода равен 1 × ставка, второй день - 2 × ставка, третий - 3 × ставка. Например, с 6 по 10 мая при ставке 1 ₽ план будет 15 ₽. В режиме ровных сумм каждый день равен указанной сумме.",
                    "Как отмечать дни" to "На вкладке «Сегодня» можно отметить текущий день. В календаре можно выбрать прошлый или будущий день внутри периода и отметить взнос вручную. Прошлые дни доступны, если включена настройка разрешения прошлых отметок.",
                    "Баланс и прогноз" to "Баланс показывает только фактически подтверждённые взносы внутри выбранного периода. Прогноз считает баланс плюс оставшийся план от сегодняшнего дня до конца периода, не догоняя пропущенные прошлые дни автоматически.",
                    "Изменение настроек" to "Новая ставка, режим и период применяются к новым расчётам. Уже подтверждённые взносы хранят свою фактическую сумму и не пересчитываются задним числом.",
                    "Зачем пользоваться" to "Так проще превратить накопления в регулярную привычку: видно, сколько нужно отложить сегодня, какие дни уже закрыты и какой результат ожидается к концу периода."
                )
            )
        }

        private fun english(locale: Locale): SavingsStrings {
            return SavingsStrings(
                language = AppLanguage.EN,
                locale = locale,
                appName = "Savings Calendar",
                appLogoContentDescription = "App logo",
                homeTab = "Today",
                calendarTab = "Calendar",
                historyTab = "History",
                settingsTab = "Settings",
                todayOutsidePeriod = "Today is outside the period",
                fixedAmountMode = "Mode: fixed amount",
                growthAmountMode = "Mode: growth by period day",
                todayContributionConfirmed = "Today's contribution is confirmed",
                makeContribution = "Add contribution",
                currentBalance = "Current balance",
                planToPeriodEnd = "Plan to period end",
                monthlyReportPrefix = "Month results",
                monthTotal = "This month",
                sinceYearStart = "Since year start",
                completedDays = "Marked days",
                monthProgress = "Month progress",
                previous = "Back",
                next = "Next",
                contributionDone = "Contribution done",
                missed = "Missed",
                today = "Today",
                amount = "Amount",
                rate = "Rate",
                outsideSavingsPeriod = "Outside savings period",
                statusConfirmed = "Status: contribution done",
                statusUnmarked = "Status: not marked",
                cancelContribution = "Cancel contribution",
                markDay = "Mark day",
                cannotMarkDay = "Cannot mark this day",
                back = "Back",
                historyEmpty = "No confirmed contributions yet. The first coin is still waiting.",
                settingsTitle = "Settings",
                helpTitle = "Help",
                helpSubtitle = "Guide to calculation, marks, balance, and forecast.",
                open = "Open",
                helpDialogTitle = "Help and guide",
                understood = "Got it",
                languageTitle = "Language",
                languageSubtitle = "System language is used by default.",
                systemLanguage = "System",
                englishLanguage = "English",
                russianLanguage = "Russian",
                fixedAmountTitle = "Fixed amount",
                baseRateTitle = "Base rate",
                dailyAmountLabel = "Daily amount",
                rateLabel = "Rate",
                currencySymbol = "Currency symbol",
                settingsAffectFuture = "Changes affect only future marks.",
                fixedAmounts = "Fixed amounts",
                fixedAmountsSubtitle = "The same amount is saved every day.",
                growthAmountsSubtitle = "Amount grows by formula: period day × base rate.",
                accumulationPeriod = "Savings period",
                start = "Start",
                end = "End",
                select = "Select",
                change = "Change",
                notificationPermissions = "Notification permissions",
                notificationsDisabled = "Notifications are disabled. Reminders and monthly reports will not be shown without permission.",
                allowNotifications = "Allow notifications",
                exactAlarmsDisabled = "Exact alarms are disabled. Reminders may not arrive at the exact selected time.",
                openAlarmSettings = "Open alarm settings",
                dailyReminders = "Daily reminders",
                dailyReminderTime = "Daily reminder time",
                monthlyReports = "Monthly reports",
                monthlyReportTime = "Monthly report time",
                allowPastDays = "Allow past-day marks",
                allowPastDaysSubtitle = "Missed days can be marked manually, but the forecast does not catch them up automatically.",
                cancelContributionQuestionText = "Cancel contribution?",
                cancelContributionForText = "Cancel contribution for %s in the amount of %d %s?",
                historyTitleText = "History: %d %s",
                dayPeriodText = "Period day #%d",
                dayYearText = "Year day #%d",
                periodText = "Period: %s - %s",
                daysOfText = "%d of %d",
                dailyNotificationTitleText = "Today is period day #%d",
                dailyNotificationText = "Add %d %s to savings.",
                monthlyNotificationTitleText = "Month results: %s",
                monthlyNotificationShortText = "This month: %d %s, year total: %d %s.",
                monthlyNotificationLongText = "Saved this month: %d %s. Since year start: %d %s. Marked days: %d of %d.",
                weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                helpSections = listOf(
                    "What this app is" to "Savings Calendar helps you save money by days. You choose the period, amount, and calculation mode, and the app shows the plan, balance, and confirmed contributions.",
                    "How to start" to "Open settings, set the base rate or fixed amount, then choose the first and last day of savings. By default, the period runs from January 1 to December 31.",
                    "How the amount is calculated" to "In growth mode, the first day of the selected period is 1 × rate, the second day is 2 × rate, the third is 3 × rate. For example, May 6 to May 10 with a 1 ₽ rate totals 15 ₽. In fixed mode, every day uses the same amount.",
                    "How to mark days" to "On the Today tab you can mark the current day. In the calendar you can choose a past or future day inside the period and mark it manually. Past days are available when past-day marks are enabled.",
                    "Balance and forecast" to "Balance shows only confirmed contributions inside the selected period. Forecast adds the remaining plan from today to the period end and does not automatically catch up missed past days.",
                    "Changing settings" to "A new rate, mode, or period applies to new calculations. Already confirmed contributions keep their actual stored amount and are not recalculated retroactively.",
                    "Why use it" to "It helps turn saving into a regular habit: you see what to save today, which days are done, and what result to expect by the end of the period."
                )
            )
        }
    }
}
