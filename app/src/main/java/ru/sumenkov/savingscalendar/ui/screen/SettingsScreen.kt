package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.ui.SavingsUiState

@Composable
fun SettingsScreen(
    state: SavingsUiState,
    onBaseRateChange: (Long) -> Unit,
    onRemindersEnabledChange: (Boolean) -> Unit,
    onMonthlyReportsEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var baseRateText by remember(state.settings.baseRate) { mutableStateOf(state.settings.baseRate.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Новая ставка влияет только на будущие отметки.")
            }
        }

        SettingSwitch(
            title = "Ежедневные напоминания",
            subtitle = "${state.settings.reminderHour.toString().padStart(2, '0')}:${state.settings.reminderMinute.toString().padStart(2, '0')}",
            checked = state.settings.remindersEnabled,
            onCheckedChange = onRemindersEnabledChange
        )

        SettingSwitch(
            title = "Месячные отчёты",
            subtitle = "Последний день месяца, ${state.settings.monthlyReportHour.toString().padStart(2, '0')}:${state.settings.monthlyReportMinute.toString().padStart(2, '0')}",
            checked = state.settings.monthlyReportsEnabled,
            onCheckedChange = onMonthlyReportsEnabledChange
        )
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
