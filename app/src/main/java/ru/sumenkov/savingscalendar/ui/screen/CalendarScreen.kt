package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(state: SavingsUiState, modifier: Modifier = Modifier) {
    val yearMonth = YearMonth.from(state.today)
    val confirmedDates = state.entries.map { it.date }.toSet()
    val days = (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() } + " ${yearMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendDot("✓", "Отложено")
            LegendDot("!", "Пропущено")
            LegendDot("•", "Сегодня")
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { date ->
                DayCell(
                    date = date,
                    confirmed = date in confirmedDates,
                    today = date == state.today
                )
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, confirmed: Boolean, today: Boolean) {
    val container = when {
        confirmed -> MaterialTheme.colorScheme.primary
        today -> MaterialTheme.colorScheme.secondary
        date.isBefore(LocalDate.now()) -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        confirmed || today -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
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
private fun LegendDot(symbol: String, label: String) {
    Text(
        text = "$symbol $label",
        style = MaterialTheme.typography.bodySmall
    )
}
