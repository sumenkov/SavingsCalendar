package ru.sumenkov.savingscalendar.ui

data class NotificationPermissionUiState(
    val notificationsGranted: Boolean = true,
    val exactAlarmsGranted: Boolean = true,
    val canRequestNotifications: Boolean = false,
    val canRequestExactAlarms: Boolean = false
)
