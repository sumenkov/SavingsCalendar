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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
            hour = state.settings.reminderHour,
            minute = state.settings.reminderMinute,
            onTimeChange = onReminderTimeChange
        )

        SettingSwitch(
            title = "Месячные отчёты",
            subtitle = "Последний день месяца, ${state.settings.monthlyReportHour.toString().padStart(2, '0')}:${state.settings.monthlyReportMinute.toString().padStart(2, '0')}",
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
    var showPicker by remember { mutableStateOf(false) }

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
            Button(onClick = { showPicker = true }) {
                Text("Изменить")
            }
        }
    }

    if (showPicker) {
        TimePickerDialogContent(
            title = title,
            hour = hour,
            minute = minute,
            onDismiss = { showPicker = false },
            onConfirm = { selectedHour, selectedMinute ->
                showPicker = false
                onTimeChange(selectedHour, selectedMinute)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    title: String,
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("Готово")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = { Text(title) },
        text = { TimePicker(state = timePickerState) }
    )
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
