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
import ru.sumenkov.savingscalendar.ui.SavingsStrings
import ru.sumenkov.savingscalendar.ui.SavingsUiState
import java.time.LocalDate

@Composable
fun HistoryScreen(
    state: SavingsUiState,
    strings: SavingsStrings,
    onDeleteDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var entryToDelete by remember { mutableStateOf<SavingsEntry?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.historyTitle(state.yearTotal, state.settings.currencySymbol),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.entries.isEmpty()) {
            Text(strings.historyEmpty)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.entries) { entry ->
                    HistoryEntryCard(
                        strings = strings,
                        entry = entry,
                        currencySymbol = state.settings.currencySymbol,
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        CancelContributionDialog(
            strings = strings,
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
private fun HistoryEntryCard(
    strings: SavingsStrings,
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
                        text = strings.monthDay(entry.date),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(strings.dayYear(entry.dayOfYear))
                }
                Text(
                    text = "${entry.amount} $currencySymbol",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Text(strings.cancelContribution)
            }
        }
    }
}

@Composable
private fun CancelContributionDialog(
    strings: SavingsStrings,
    date: LocalDate,
    amount: Long,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dateText = strings.fullDate(date)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.cancelContributionQuestion()) },
        text = { Text(strings.cancelContributionFor(dateText, amount, currencySymbol)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(strings.cancelContribution)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(strings.back)
            }
        }
    )
}
