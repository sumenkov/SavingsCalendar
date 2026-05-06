@file:OptIn(ExperimentalMaterial3Api::class)

package ru.sumenkov.savingscalendar.ui.screen

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import ru.sumenkov.savingscalendar.ui.NotificationPermissionUiState
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingsScreen(
    state: SavingsUiState,
    notificationPermissionState: NotificationPermissionUiState,
    onBaseRateChange: (Long) -> Unit,
    onRemindersEnabledChange: (Boolean) -> Unit,
    onMonthlyReportsEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    onMonthlyReportTimeChange: (Int, Int) -> Unit,
    onAllowPastDaysChange: (Boolean) -> Unit,
    onCurrencySymbolChange: (String) -> Unit,
    onAccumulationStartDateChange: (LocalDate) -> Unit,
    onAccumulationEndDateChange: (LocalDate) -> Unit,
    onAmountModeChange: (SavingsAmountMode) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var baseRateText by remember(state.settings.baseRate) { mutableStateOf(state.settings.baseRate.toString()) }
    var currencyText by remember(state.settings.currencySymbol) {
        mutableStateOf(state.settings.currencySymbol)
    }
    var showHelp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        HelpCard(onOpen = { showHelp = true })

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                        "Ровная сумма"
                    } else {
                        "Базовая ставка"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = baseRateText,
                    onValueChange = { value ->
                        baseRateText = value.filter { it.isDigit() }
                        baseRateText.toLongOrNull()?.let(onBaseRateChange)
                    },
                    label = {
                        Text(
                            if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                                "Сумма в день, ${state.settings.currencySymbol}"
                            } else {
                                "Ставка, ${state.settings.currencySymbol}"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currencyText,
                    onValueChange = { value ->
                        currencyText = value.take(4)
                        if (currencyText.isNotBlank()) {
                            onCurrencySymbolChange(currencyText)
                        }
                    },
                    label = { Text("Символ валюты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Изменение влияет только на будущие отметки.")
            }
        }

        SettingSwitch(
            title = "Ровные суммы",
            subtitle = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                "Каждый день откладывается одна и та же сумма."
            } else {
                "Сумма растёт по формуле: день периода × базовая ставка."
            },
            checked = state.settings.amountMode == SavingsAmountMode.FIXED,
            onCheckedChange = { fixed ->
                onAmountModeChange(
                    if (fixed) SavingsAmountMode.FIXED else SavingsAmountMode.DAILY_GROWTH
                )
            }
        )

        AccumulationPeriodCard(
            state = state,
            onStartDateChange = onAccumulationStartDateChange,
            onEndDateChange = onAccumulationEndDateChange
        )

        PermissionCard(
            state = notificationPermissionState,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onOpenExactAlarmSettings = onOpenExactAlarmSettings
        )

        SettingSwitch(
            title = "Ежедневные напоминания",
            subtitle = formatTime(state.settings.reminderHour, state.settings.reminderMinute),
            checked = state.settings.remindersEnabled,
            onCheckedChange = onRemindersEnabledChange
        )

        TimeSettingCard(
            title = "Время ежедневного напоминания",
            hour = state.settings.reminderHour,
            minute = state.settings.reminderMinute,
            onTimeChange = onReminderTimeChange
        )

        SettingSwitch(
            title = "Месячные отчёты",
            subtitle = "Последний день месяца, ${formatTime(state.settings.monthlyReportHour, state.settings.monthlyReportMinute)}",
            checked = state.settings.monthlyReportsEnabled,
            onCheckedChange = onMonthlyReportsEnabledChange
        )

        TimeSettingCard(
            title = "Время месячного отчёта",
            hour = state.settings.monthlyReportHour,
            minute = state.settings.monthlyReportMinute,
            onTimeChange = onMonthlyReportTimeChange
        )

        SettingSwitch(
            title = "Разрешить отметки прошлых дней",
            subtitle = "Пропущенные дни можно отметить вручную, но прогноз их не догоняет автоматически.",
            checked = state.settings.allowPastDays,
            onCheckedChange = onAllowPastDaysChange
        )
    }

    if (showHelp) {
        SavingsHelpDialog(onDismiss = { showHelp = false })
    }
}

@Composable
private fun HelpCard(onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Помощь", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Инструкция по расчёту, отметкам, балансу и прогнозу.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onOpen) {
                Text("Открыть")
            }
        }
    }
}

@Composable
private fun SavingsHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Помощь и инструкция") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                helpSections.forEach { (title, body) ->
                    InstructionSection(title = title, body = body)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Понятно")
            }
        }
    )
}

@Composable
private fun InstructionSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AccumulationPeriodCard(
    state: SavingsUiState,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    val minDate = state.today.minusYears(1)
    val maxDate = state.today.plusYears(1)
    val startDate = state.settings.accumulationStartDate()
    val endDate = state.settings.accumulationEndDate()
    val startMaxDate = maxOf(minDate, minOf(maxDate, endDate))
    val endMinDate = minOf(maxDate, maxOf(minDate, startDate))
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Период накоплений", style = MaterialTheme.typography.titleMedium)
            Text(
                "Доступный диапазон: ${minDate.format(formatter)} - ${maxDate.format(formatter)}. " +
                    "Начало нельзя выбрать позже конца.",
                style = MaterialTheme.typography.bodyMedium
            )
            DateSettingRow(
                title = "Начало",
                date = startDate,
                minDate = minDate,
                maxDate = startMaxDate,
                onDateChange = onStartDateChange
            )
            DateSettingRow(
                title = "Конец",
                date = endDate,
                minDate = endMinDate,
                maxDate = maxDate,
                onDateChange = onEndDateChange
            )
        }
    }
}

@Composable
private fun DateSettingRow(
    title: String,
    date: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(date.format(formatter), style = MaterialTheme.typography.bodyMedium)
        }
        Button(onClick = { showDatePicker = true }) {
            Text("Выбрать")
        }
    }

    if (showDatePicker) {
        SavingsDatePickerDialog(
            initialDate = date,
            minDate = minDate,
            maxDate = maxDate,
            onDismiss = { showDatePicker = false },
            onDateChange = onDateChange
        )
    }
}

@Composable
private fun SavingsDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    onDismiss: () -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    val safeInitialDate = initialDate.coerceIn(minDate, maxDate)
    val selectableDates = remember(minDate, maxDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toUtcLocalDate()
                return !date.isBefore(minDate) && !date.isAfter(maxDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in minDate.year..maxDate.year
            }
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = safeInitialDate.toUtcMillis(),
        initialDisplayedMonthMillis = safeInitialDate.toUtcMillis(),
        yearRange = minDate.year..maxDate.year,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                        ?.toUtcLocalDate()
                        ?.coerceIn(minDate, maxDate)
                        ?: initialDate
                    onDateChange(selectedDate)
                    onDismiss()
                }
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Назад")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun PermissionCard(
    state: NotificationPermissionUiState,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    if (state.notificationsGranted && state.exactAlarmsGranted) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Разрешения уведомлений", style = MaterialTheme.typography.titleMedium)

            if (!state.notificationsGranted) {
                Text("Уведомления выключены. Без разрешения напоминания и месячные отчёты не будут показаны.")
                if (state.canRequestNotifications) {
                    Button(onClick = onRequestNotificationPermission) {
                        Text("Разрешить уведомления")
                    }
                }
            }

            if (!state.exactAlarmsGranted) {
                Text("Точные будильники выключены. Напоминания могут приходить не строго в заданное время.")
                if (state.canRequestExactAlarms) {
                    Button(onClick = onOpenExactAlarmSettings) {
                        Text("Открыть настройки будильников")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSettingCard(
    title: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(formatTime(hour, minute), style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, selectedHour, selectedMinute ->
                            onTimeChange(selectedHour, selectedMinute)
                        },
                        hour,
                        minute,
                        true
                    ).show()
                }
            ) {
                Text("Изменить")
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun LocalDate.toUtcMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun Long.toUtcLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun LocalDate.coerceIn(minDate: LocalDate, maxDate: LocalDate): LocalDate {
    return when {
        isBefore(minDate) -> minDate
        isAfter(maxDate) -> maxDate
        else -> this
    }
}

private val helpSections = listOf(
    "Что это за приложение" to "Календарь накоплений помогает откладывать деньги по дням. Вы выбираете период, сумму и режим расчёта, а приложение показывает план, баланс и отмеченные взносы.",
    "Как начать" to "Откройте настройки, задайте базовую ставку или ровную сумму, выберите первый и последний день накоплений. По умолчанию период длится с 1 января по 31 декабря.",
    "Как считается сумма" to "В режиме роста первый день выбранного периода равен 1 × ставка, второй день - 2 × ставка, третий - 3 × ставка. Например, с 6 по 10 мая при ставке 1 ₽ план будет 15 ₽. В режиме ровных сумм каждый день равен указанной сумме.",
    "Как отмечать дни" to "На вкладке «Сегодня» можно отметить текущий день. В календаре можно выбрать прошлый или будущий день внутри периода и отметить взнос вручную. Прошлые дни доступны, если включена настройка разрешения прошлых отметок.",
    "Баланс и прогноз" to "Баланс показывает только фактически подтверждённые взносы внутри выбранного периода. Прогноз считает баланс плюс оставшийся план от сегодняшнего дня до конца периода, не догоняя пропущенные прошлые дни автоматически.",
    "Изменение настроек" to "Новая ставка, режим и период применяются к новым расчётам. Уже подтверждённые взносы хранят свою фактическую сумму и не пересчитываются задним числом.",
    "Зачем пользоваться" to "Так проще превратить накопления в регулярную привычку: видно, сколько нужно отложить сегодня, какие дни уже закрыты и какой результат ожидается к концу периода."
)
