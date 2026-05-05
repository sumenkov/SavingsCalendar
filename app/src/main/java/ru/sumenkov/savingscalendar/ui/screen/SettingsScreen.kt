package ru.sumenkov.savingscalendar.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ru.sumenkov.savingscalendar.data.settings.AppLanguage
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import ru.sumenkov.savingscalendar.ui.NotificationPermissionUiState
import ru.sumenkov.savingscalendar.ui.SavingsStrings
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun SettingsScreen(
    state: SavingsUiState,
    strings: SavingsStrings,
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
    onLanguageChange: (AppLanguage) -> Unit,
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
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        HelpCard(strings = strings, onOpen = { showHelp = true })

        LanguageCard(
            state = state,
            strings = strings,
            onLanguageChange = onLanguageChange
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                        strings.fixedAmountTitle
                    } else {
                        strings.baseRateTitle
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
                                "${strings.dailyAmountLabel}, ${state.settings.currencySymbol}"
                            } else {
                                "${strings.rateLabel}, ${state.settings.currencySymbol}"
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
                    label = { Text(strings.currencySymbol) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(strings.settingsAffectFuture)
            }
        }

        SettingSwitch(
            title = strings.fixedAmounts,
            subtitle = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                strings.fixedAmountsSubtitle
            } else {
                strings.growthAmountsSubtitle
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
            strings = strings,
            onStartDateChange = onAccumulationStartDateChange,
            onEndDateChange = onAccumulationEndDateChange
        )

        PermissionCard(
            state = notificationPermissionState,
            strings = strings,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onOpenExactAlarmSettings = onOpenExactAlarmSettings
        )

        SettingSwitch(
            title = strings.dailyReminders,
            subtitle = formatTime(state.settings.reminderHour, state.settings.reminderMinute),
            checked = state.settings.remindersEnabled,
            onCheckedChange = onRemindersEnabledChange
        )

        TimeSettingCard(
            title = strings.dailyReminderTime,
            hour = state.settings.reminderHour,
            minute = state.settings.reminderMinute,
            strings = strings,
            onTimeChange = onReminderTimeChange
        )

        SettingSwitch(
            title = strings.monthlyReports,
            subtitle = formatTime(state.settings.monthlyReportHour, state.settings.monthlyReportMinute),
            checked = state.settings.monthlyReportsEnabled,
            onCheckedChange = onMonthlyReportsEnabledChange
        )

        TimeSettingCard(
            title = strings.monthlyReportTime,
            hour = state.settings.monthlyReportHour,
            minute = state.settings.monthlyReportMinute,
            strings = strings,
            onTimeChange = onMonthlyReportTimeChange
        )

        SettingSwitch(
            title = strings.allowPastDays,
            subtitle = strings.allowPastDaysSubtitle,
            checked = state.settings.allowPastDays,
            onCheckedChange = onAllowPastDaysChange
        )
    }

    if (showHelp) {
        SavingsHelpDialog(strings = strings, onDismiss = { showHelp = false })
    }
}

@Composable
private fun HelpCard(strings: SavingsStrings, onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.helpTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    strings.helpSubtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onOpen) {
                Text(strings.open)
            }
        }
    }
}

@Composable
private fun LanguageCard(
    state: SavingsUiState,
    strings: SavingsStrings,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.languageTitle, style = MaterialTheme.typography.titleMedium)
            Text(strings.languageSubtitle, style = MaterialTheme.typography.bodyMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    Button(
                        onClick = { onLanguageChange(language) },
                        enabled = state.settings.language != language,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.languageOption(language))
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsHelpDialog(strings: SavingsStrings, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.helpDialogTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                strings.helpSections.forEach { (title, body) ->
                    InstructionSection(title = title, body = body)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.understood)
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
    strings: SavingsStrings,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    val year = state.today.year
    val minDate = LocalDate.of(year, 1, 1)
    val maxDate = LocalDate.of(year, 12, 31)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.accumulationPeriod, style = MaterialTheme.typography.titleMedium)
            DateSettingRow(
                title = strings.start,
                date = state.settings.accumulationStartDate(year),
                minDate = minDate,
                maxDate = maxDate,
                strings = strings,
                onDateChange = onStartDateChange
            )
            DateSettingRow(
                title = strings.end,
                date = state.settings.accumulationEndDate(year),
                minDate = minDate,
                maxDate = maxDate,
                strings = strings,
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
    strings: SavingsStrings,
    onDateChange: (LocalDate) -> Unit
) {
    val context = LocalContext.current.withLocale(strings.locale)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(strings.fullDate(date), style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        onDateChange(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                    },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth
                ).apply {
                    datePicker.minDate = minDate.toPickerMillis()
                    datePicker.maxDate = maxDate.toPickerMillis()
                }.show()
            }
        ) {
            Text(strings.select)
        }
    }
}

@Composable
private fun PermissionCard(
    state: NotificationPermissionUiState,
    strings: SavingsStrings,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    if (state.notificationsGranted && state.exactAlarmsGranted) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.notificationPermissions, style = MaterialTheme.typography.titleMedium)

            if (!state.notificationsGranted) {
                Text(strings.notificationsDisabled)
                if (state.canRequestNotifications) {
                    Button(onClick = onRequestNotificationPermission) {
                        Text(strings.allowNotifications)
                    }
                }
            }

            if (!state.exactAlarmsGranted) {
                Text(strings.exactAlarmsDisabled)
                if (state.canRequestExactAlarms) {
                    Button(onClick = onOpenExactAlarmSettings) {
                        Text(strings.openAlarmSettings)
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
    strings: SavingsStrings,
    onTimeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current.withLocale(strings.locale)

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
                Text(strings.change)
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

private fun LocalDate.toPickerMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Context.withLocale(locale: Locale): Context {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}
