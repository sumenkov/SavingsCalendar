package ru.sumenkov.savingscalendar.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavingsApp(
    viewModel: SavingsViewModel,
    notificationPermissionState: NotificationPermissionUiState,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(AppTab.Home.ordinal) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) {
        AppTab.entries.size
    }

    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            selectedTabIndex = page
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == tab.ordinal,
                        onClick = { selectedTabIndex = tab.ordinal },
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
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        HorizontalPager(
            state = pagerState,
            modifier = modifier,
            key = { page -> AppTab.entries[page].name }
        ) { page ->
            when (AppTab.entries[page]) {
                AppTab.Home -> HomeScreen(
                    state = state,
                    onConfirmToday = viewModel::confirmToday,
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.Calendar -> CalendarScreen(
                    state = state,
                    onConfirmDate = viewModel::confirmDate,
                    onDeleteDate = viewModel::deleteDate,
                    amountForDate = viewModel::plannedAmountFor,
                    dayNumberForDate = viewModel::dayNumberInPeriodFor,
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.History -> HistoryScreen(
                    state = state,
                    onDeleteDate = viewModel::deleteDate,
                    modifier = Modifier.fillMaxSize()
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
                    onAccumulationStartDateChange = viewModel::setAccumulationStartDate,
                    onAccumulationEndDateChange = viewModel::setAccumulationEndDate,
                    onAmountModeChange = viewModel::setAmountMode,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onOpenExactAlarmSettings = onOpenExactAlarmSettings,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
