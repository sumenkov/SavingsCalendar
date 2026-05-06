package ru.sumenkov.savingscalendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: SavingsUiState,
    onDeleteDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var entryToDelete by remember { mutableStateOf<SavingsEntry?>(null) }
    val groupedEntries = remember(state.entries) {
        state.entries
            .groupBy { YearMonth.from(it.date) }
            .toList()
            .sortedByDescending { it.first }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "История: ${state.historyTotal} ${state.settings.currencySymbol}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.entries.isEmpty()) {
            Text("Пока нет подтверждённых взносов. Первая монетка ещё ждёт своего часа.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupedEntries.forEach { (month, entries) ->
                    item {
                        MonthHeader(
                            month = month,
                            total = entries.sumOf { it.amount },
                            currencySymbol = state.settings.currencySymbol
                        )
                    }
                    items(entries) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            currencySymbol = state.settings.currencySymbol,
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        CancelContributionDialog(
            date = entry.date,
            amount = entry.amount,
            currencySymbol = state.settings.currencySymbol,
            onDismiss = { entryToDelete = null },
            onConfirm = {
                entryToDelete = null
                onDeleteDate(entry.date)
            }
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    total: Long,
    currencySymbol: String
) {
    val title = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
        .replaceFirstChar { it.uppercase() }

    Text(
        text = "$title: $total $currencySymbol",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun HistoryEntryCard(
    entry: SavingsEntry,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = entry.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Ставка: ${entry.baseRate} $currencySymbol")
                }
                Text(
                    text = "${entry.amount} $currencySymbol",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
    val dateText = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отменить взнос?") },
        text = { Text("Отменить взнос за $dateText на сумму $amount $currencySymbol?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Отменить взнос")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Назад")
            }
        }
    )
}
