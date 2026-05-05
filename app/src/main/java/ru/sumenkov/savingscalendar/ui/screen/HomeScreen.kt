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
import ru.sumenkov.savingscalendar.ui.SavingsStrings
import ru.sumenkov.savingscalendar.ui.SavingsUiState

@Composable
fun HomeScreen(
    state: SavingsUiState,
    strings: SavingsStrings,
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
                    contentDescription = strings.appLogoContentDescription,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { TodayCard(state = state, strings = strings, onConfirmToday = onConfirmToday) }
        item { TotalsCard(state = state, strings = strings) }
        item { MonthlyReportCard(state = state, strings = strings) }
    }
}

@Composable
private fun TodayCard(
    state: SavingsUiState,
    strings: SavingsStrings,
    onConfirmToday: () -> Unit
) {
    val todayInPeriod = state.settings.isDateInAccumulationPeriod(state.today)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = strings.fullDate(state.today),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = state.todayDayNumberInPeriod?.let(strings::dayPeriod)
                    ?: strings.todayOutsidePeriod,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (state.settings.amountMode == SavingsAmountMode.FIXED) {
                    strings.fixedAmountMode
                } else {
                    strings.growthAmountMode
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${state.todayAmount} ${state.settings.currencySymbol}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onConfirmToday,
                enabled = !state.todayConfirmed && todayInPeriod,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        state.todayConfirmed -> strings.todayContributionConfirmed
                        !todayInPeriod -> strings.todayOutsidePeriod
                        else -> strings.makeContribution
                    }
                )
            }
        }
    }
}

@Composable
private fun TotalsCard(state: SavingsUiState, strings: SavingsStrings) {
    val periodStart = strings.periodDate(state.settings.accumulationStartDate(state.today.year))
    val periodEnd = strings.periodDate(state.settings.accumulationEndDate(state.today.year))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.currentBalance, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${state.yearTotal} ${state.settings.currencySymbol}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(strings.planToPeriodEnd)
            Text(
                text = "${state.forecastToEndOfPeriod} ${state.settings.currencySymbol}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            Text(strings.period(periodStart, periodEnd))
        }
    }
}

@Composable
private fun MonthlyReportCard(state: SavingsUiState, strings: SavingsStrings) {
    val report = state.monthlyReport ?: return
    val monthName = strings.monthName(report.yearMonth)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${strings.monthlyReportPrefix}: $monthName", style = MaterialTheme.typography.titleMedium)
            ReportLine(strings.monthTotal, "${report.monthTotal} ${state.settings.currencySymbol}")
            ReportLine(strings.sinceYearStart, "${report.yearTotal} ${state.settings.currencySymbol}")
            ReportLine(strings.completedDays, strings.daysOf(report.completedDaysInMonth, report.daysInMonth))
            ReportLine(strings.monthProgress, "${report.completionPercent}%")
        }
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
