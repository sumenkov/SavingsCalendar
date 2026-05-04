package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.ui.NotificationPermissionUiState
import ru.sumenkov.savingscalendar.ui.SavingsUiState

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
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var baseRateText by remember(state.settings.baseRate) { mutableStateOf(state.settings.baseRate.toString()) }
    var reminderTimeText by remember(state.settings.reminderHour, state.settings.reminderMinute) {
        mutableStateOf(formatTime(state.settings.reminderHour, state.settings.reminderMinute))
    }
    var monthlyReportTimeText by remember(state.settings.monthlyReportHour, state.settings.monthlyReportMinute) {
        mutableStateOf(formatTime(state.settings.monthlyReportHour, state.settings.monthlyReportMinute))
    }
    var currencyText by remember(state.settings.currencySymbol) {
        mutableStateOf(state.settings.currencySymbol)
    }

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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Базовая ставка", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = baseRateText,
                    onValueChange = { value ->
                        baseRateText = value.filter { it.isDigit() }
                        baseRateText.toLongOrNull()?.let(onBaseRateChange)
                    },
                    label = { Text("Ставка, ${state.settings.currencySymbol}") },
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
                Text("Новая ставка влияет только на будущие отметки.")
            }
        }

        PermissionCard(
            state = notificationPermissionState,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onOpenExactAlarmSettings = onOpenExactAlarmSettings
        )

        SettingSwitch(
            title = "Ежедневные напоминания",
            subtitle = "${state.settings.reminderHour.toString().padStart(2, '0')}:${state.settings.reminderMinute.toString().padStart(2, '0')}",
            checked = state.settings.remindersEnabled,
            onCheckedChange = onRemindersEnabledChange
        )

        TimeSettingCard(
            title = "Время ежедневного напоминания",
            value = reminderTimeText,
            onValueChange = { value ->
                reminderTimeText = value.filter { it.isDigit() || it == ':' }.take(5)
                parseTime(reminderTimeText)?.let { (hour, minute) ->
                    onReminderTimeChange(hour, minute)
                }
            }
        )

        SettingSwitch(
            title = "Месячные отчёты",
            subtitle = "Последний день месяца, ${state.settings.monthlyReportHour.toString().padStart(2, '0')}:${state.settings.monthlyReportMinute.toString().padStart(2, '0')}",
            checked = state.settings.monthlyReportsEnabled,
            onCheckedChange = onMonthlyReportsEnabledChange
        )

        TimeSettingCard(
            title = "Время месячного отчёта",
            value = monthlyReportTimeText,
            onValueChange = { value ->
                monthlyReportTimeText = value.filter { it.isDigit() || it == ':' }.take(5)
                parseTime(monthlyReportTimeText)?.let { (hour, minute) ->
                    onMonthlyReportTimeChange(hour, minute)
                }
            }
        )

        SettingSwitch(
            title = "Разрешить отметки прошлых дней",
            subtitle = "Пропущенные дни можно отметить вручную, но прогноз их не догоняет автоматически.",
            checked = state.settings.allowPastDays,
            onCheckedChange = onAllowPastDaysChange
        )
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
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Формат 20:30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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

private fun parseTime(value: String): Pair<Int, Int>? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return if (hour in 0..23 && minute in 0..59) hour to minute else null
}
