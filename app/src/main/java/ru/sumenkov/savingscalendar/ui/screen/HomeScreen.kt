package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.R
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    state: SavingsUiState,
    onConfirmToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Логотип приложения",
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Календарь накоплений",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { TodayCard(state = state, onConfirmToday = onConfirmToday) }
        item { TotalsCard(state = state) }
        item { MonthlyReportCard(state = state) }
    }
}

@Composable
private fun TodayCard(state: SavingsUiState, onConfirmToday: () -> Unit) {
    val todayInPeriod = state.settings.isDateInAccumulationPeriod(state.today)
    val periodStatus = periodStatusText(state.today, state.settings.accumulationStartDate(), state.settings.accumulationEndDate())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = state.today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = state.todayDayNumberInPeriod?.let { "День периода №$it" } ?: "Сегодня вне периода",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                    "Режим: ровная сумма"
                } else {
                    "Режим: рост по дню периода"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (todayInPeriod) {
                    "${state.todayAmount} ${state.settings.currencySymbol}"
                } else {
                    "Взнос не нужен"
                },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (!todayInPeriod) {
                Text(
                    text = periodStatus,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onConfirmToday,
                enabled = !state.todayConfirmed && todayInPeriod,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        !todayInPeriod -> "Сегодня вне периода"
                        state.todayConfirmed -> "Взнос за сегодня сделан"
                        else -> "Сделать взнос"
                    }
                )
            }
        }
    }
}

@Composable
private fun TotalsCard(state: SavingsUiState) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val periodStart = state.settings.accumulationStartDate().format(formatter)
    val periodEnd = state.settings.accumulationEndDate().format(formatter)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Баланс периода", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${state.periodTotal} ${state.settings.currencySymbol}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("Прогноз до конца периода")
            Text(
                text = "${state.forecastToEndOfPeriod} ${state.settings.currencySymbol}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            Text("Период: $periodStart - $periodEnd")
        }
    }
}

@Composable
private fun MonthlyReportCard(state: SavingsUiState) {
    val report = state.monthlyReport ?: return
    val monthName = report.yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Итоги месяца: $monthName", style = MaterialTheme.typography.titleMedium)
            if (report.plannedDaysInMonth == 0) {
                Text("В этом месяце нет дней выбранного периода.")
                ReportLine("За период", "${report.periodTotal} ${state.settings.currencySymbol}")
                return@Column
            }
            ReportLine("За месяц", "${report.monthTotal} ${state.settings.currencySymbol}")
            ReportLine("За период", "${report.periodTotal} ${state.settings.currencySymbol}")
            ReportLine("Отмечено дней периода", "${report.completedDaysInMonth} из ${report.plannedDaysInMonth}")
            ReportLine("Прогресс месяца в периоде", "${report.completionPercent}%")
        }
    }
}

private fun periodStatusText(today: LocalDate, startDate: LocalDate, endDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    return when {
        today.isBefore(startDate) -> "Период начнётся ${startDate.format(formatter)}"
        today.isAfter(endDate) -> "Период завершён ${endDate.format(formatter)}"
        else -> "Сегодня вне периода"
    }
}

@Composable
private fun ReportLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
