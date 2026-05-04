package ru.sumenkov.savingscalendar.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import ru.sumenkov.savingscalendar.R
import ru.sumenkov.savingscalendar.ui.screen.CalendarScreen
import ru.sumenkov.savingscalendar.ui.screen.HistoryScreen
import ru.sumenkov.savingscalendar.ui.screen.HomeScreen
import ru.sumenkov.savingscalendar.ui.screen.SettingsScreen

enum class AppTab(val title: String, val iconRes: Int) {
    Home("Сегодня", R.drawable.ic_tab_today),
    Calendar("Календарь", R.drawable.ic_tab_calendar),
    History("История", R.drawable.ic_tab_history),
    Settings("Настройки", R.drawable.ic_tab_settings)
}

@Composable
fun SavingsApp(
    viewModel: SavingsViewModel,
    notificationPermissionState: NotificationPermissionUiState,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = null
                            )
                        },
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
            AppTab.Calendar -> CalendarScreen(
                state = state,
                onConfirmDate = viewModel::confirmDate,
                onDeleteDate = viewModel::deleteDate,
                amountForDate = viewModel::plannedAmountFor,
                modifier = modifier
            )
            AppTab.History -> HistoryScreen(
                state = state,
                onDeleteDate = viewModel::deleteDate,
                modifier = modifier
            )
            AppTab.Settings -> SettingsScreen(
                state = state,
                notificationPermissionState = notificationPermissionState,
                onBaseRateChange = viewModel::updateBaseRate,
                onRemindersEnabledChange = viewModel::setRemindersEnabled,
                onMonthlyReportsEnabledChange = viewModel::setMonthlyReportsEnabled,
                onReminderTimeChange = viewModel::setReminderTime,
                onMonthlyReportTimeChange = viewModel::setMonthlyReportTime,
                onAllowPastDaysChange = viewModel::setAllowPastDays,
                onCurrencySymbolChange = viewModel::setCurrencySymbol,
                onRequestNotificationPermission = onRequestNotificationPermission,
                onOpenExactAlarmSettings = onOpenExactAlarmSettings,
                modifier = modifier
            )
        }
    }
}
