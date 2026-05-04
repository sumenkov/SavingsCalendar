package ru.sumenkov.savingscalendar.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.sumenkov.savingscalendar.ui.screen.CalendarScreen
import ru.sumenkov.savingscalendar.ui.screen.HistoryScreen
import ru.sumenkov.savingscalendar.ui.screen.HomeScreen
import ru.sumenkov.savingscalendar.ui.screen.SettingsScreen

enum class AppTab(val title: String) {
    Home("Сегодня"),
    Calendar("Календарь"),
    History("История"),
    Settings("Настройки")
}

@Composable
fun SavingsApp(viewModel: SavingsViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.title.take(1)) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (selectedTab) {
            AppTab.Home -> HomeScreen(
                state = state,
                onConfirmToday = viewModel::confirmToday,
                modifier = modifier
            )
            AppTab.Calendar -> CalendarScreen(state = state, modifier = modifier)
            AppTab.History -> HistoryScreen(state = state, modifier = modifier)
            AppTab.Settings -> SettingsScreen(
                state = state,
                onBaseRateChange = viewModel::updateBaseRate,
                onRemindersEnabledChange = viewModel::setRemindersEnabled,
                onMonthlyReportsEnabledChange = viewModel::setMonthlyReportsEnabled,
                modifier = modifier
            )
        }
    }
}
