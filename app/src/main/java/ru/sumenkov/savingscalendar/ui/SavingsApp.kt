package ru.sumenkov.savingscalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

private const val TAB_ANIMATION_MILLIS = 260

@Composable
fun SavingsApp(
    viewModel: SavingsViewModel,
    notificationPermissionState: NotificationPermissionUiState,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    val swipeThreshold = with(LocalDensity.current) { 72.dp.toPx() }

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
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .swipeTabNavigation(
                selectedTab = selectedTab,
                swipeThreshold = swipeThreshold,
                onTabSelected = { selectedTab = it }
            )
        AnimatedContent(
            targetState = selectedTab,
            modifier = modifier,
            transitionSpec = {
                tabSlideTransition(forward = targetState.ordinal > initialState.ordinal)
            },
            label = "MainTabTransition"
        ) { tab ->
            when (tab) {
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

private fun tabSlideTransition(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    return (
        slideInHorizontally(animationSpec = tween(durationMillis = TAB_ANIMATION_MILLIS)) { fullWidth ->
            fullWidth * direction
        } +
            fadeIn(animationSpec = tween(durationMillis = TAB_ANIMATION_MILLIS))
        ).togetherWith(
        slideOutHorizontally(animationSpec = tween(durationMillis = TAB_ANIMATION_MILLIS)) { fullWidth ->
            -fullWidth * direction
        } +
            fadeOut(animationSpec = tween(durationMillis = TAB_ANIMATION_MILLIS))
    )
}

private fun Modifier.swipeTabNavigation(
    selectedTab: AppTab,
    swipeThreshold: Float,
    onTabSelected: (AppTab) -> Unit
): Modifier {
    return pointerInput(selectedTab, swipeThreshold) {
        var horizontalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { horizontalDrag = 0f },
            onHorizontalDrag = { _, dragAmount ->
                horizontalDrag += dragAmount
            },
            onDragEnd = {
                when {
                    horizontalDrag <= -swipeThreshold -> selectedTab.next()?.let(onTabSelected)
                    horizontalDrag >= swipeThreshold -> selectedTab.previous()?.let(onTabSelected)
                }
            },
            onDragCancel = { horizontalDrag = 0f }
        )
    }
}

private fun AppTab.next(): AppTab? {
    val nextIndex = ordinal + 1
    return AppTab.entries.getOrNull(nextIndex)
}

private fun AppTab.previous(): AppTab? {
    val previousIndex = ordinal - 1
    return AppTab.entries.getOrNull(previousIndex)
}
