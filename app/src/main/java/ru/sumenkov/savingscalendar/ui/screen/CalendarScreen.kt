package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.domain.calendarRangeAround
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    state: SavingsUiState,
    onConfirmDate: (LocalDate) -> Unit,
    onDeleteDate: (LocalDate) -> Unit,
    amountForDate: (LocalDate) -> Long,
    dayNumberForDate: (LocalDate) -> Int?,
    modifier: Modifier = Modifier
) {
    var yearMonth by remember(state.today.year) { mutableStateOf(YearMonth.from(state.today)) }
    var selectedDate by remember(state.today) { mutableStateOf<LocalDate?>(state.today) }
    var dateToDelete by remember { mutableStateOf<LocalDate?>(null) }
    val confirmedDates = state.entries.map { it.date }.toSet()
    val accumulationStartDate = state.settings.accumulationStartDate(yearMonth.year)
    val accumulationEndDate = state.settings.accumulationEndDate(yearMonth.year)
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val days = List(firstDayOffset) { null } + (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }
    val calendarRange = calendarRangeAround(state.today)
    val canGoPrevious = calendarRange.canGoPrevious(yearMonth)
    val canGoNext = calendarRange.canGoNext(yearMonth)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    yearMonth = yearMonth.minusMonths(1)
                    selectedDate = yearMonth.atDay(1)
                },
                enabled = canGoPrevious
            ) {
                Text("Назад")
            }
            Text(
                text = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                    .replaceFirstChar { it.uppercase() } + " ${yearMonth.year}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    yearMonth = yearMonth.plusMonths(1)
                    selectedDate = yearMonth.atDay(1)
                },
                enabled = canGoNext
            ) {
                Text("Вперёд")
            }
        }

        WeekHeader()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { date ->
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    DayCell(
                        date = date,
                        confirmed = date in confirmedDates,
                        today = date == state.today,
                        selected = date == selectedDate,
                        currentDate = state.today,
                        inSavingsPeriod = !date.isBefore(accumulationStartDate) &&
                            !date.isAfter(accumulationEndDate),
                        onClick = { selectedDate = date }
                    )
                }
            }
        }

        selectedDate?.let { date ->
            val entry = state.entries.firstOrNull { it.date == date }
            val inSavingsPeriod = state.settings.isDateInAccumulationPeriod(date)
            DayDetailsCard(
                date = date,
                amount = entry?.amount ?: if (inSavingsPeriod) amountForDate(date) else 0L,
                dayNumberInPeriod = dayNumberForDate(date),
                currencySymbol = state.settings.currencySymbol,
                confirmed = entry != null,
                baseRate = entry?.baseRate ?: state.settings.baseRate,
                inSavingsPeriod = inSavingsPeriod,
                canConfirm = entry == null &&
                    inSavingsPeriod &&
                    (!date.isBefore(state.today) || state.settings.allowPastDays),
                onConfirm = { onConfirmDate(date) },
                onDelete = { dateToDelete = date }
            )
        }
    }

    dateToDelete?.let { date ->
        val entry = state.entries.firstOrNull { it.date == date }
        CancelContributionDialog(
            date = date,
            amount = entry?.amount ?: amountForDate(date),
            currencySymbol = state.settings.currencySymbol,
            onDismiss = { dateToDelete = null },
            onConfirm = {
                dateToDelete = null
                onDeleteDate(date)
            }
        )
    }
}

@Composable
private fun WeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { label ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    confirmed: Boolean,
    today: Boolean,
    selected: Boolean,
    currentDate: LocalDate,
    inSavingsPeriod: Boolean,
    onClick: () -> Unit
) {
    val container = when {
        confirmed -> MaterialTheme.colorScheme.primary
        today -> MaterialTheme.colorScheme.secondary
        selected -> MaterialTheme.colorScheme.tertiaryContainer
        !inSavingsPeriod -> MaterialTheme.colorScheme.surfaceVariant
        date.isBefore(currentDate) -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        confirmed -> MaterialTheme.colorScheme.onPrimary
        today -> MaterialTheme.colorScheme.onSecondary
        selected -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DayDetailsCard(
    date: LocalDate,
    amount: Long,
    dayNumberInPeriod: Int?,
    currencySymbol: String,
    confirmed: Boolean,
    baseRate: Long,
    inSavingsPeriod: Boolean,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(dateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (dayNumberInPeriod != null) {
                    "День периода №$dayNumberInPeriod"
                } else {
                    "День года №${date.dayOfYear}"
                }
            )
            Text("Сумма: $amount $currencySymbol")
            Text("Ставка: $baseRate $currencySymbol")
            if (!inSavingsPeriod) {
                Text("День вне периода накоплений.")
            }
            Text(if (confirmed) "Статус: взнос сделан" else "Статус: не отмечено")
            if (confirmed) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Отменить взнос")
                }
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = canConfirm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            canConfirm -> "Отметить день"
                            !inSavingsPeriod -> "Вне периода накоплений"
                            else -> "Нельзя отметить этот день"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelContributionDialog(
    date: LocalDate,
    amount: Long,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dateText = date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отменить взнос?") },
        text = { Text("Отменить взнос за $dateText на сумму $amount $currencySymbol?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Отменить взнос")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Назад")
            }
        }
    )
}
