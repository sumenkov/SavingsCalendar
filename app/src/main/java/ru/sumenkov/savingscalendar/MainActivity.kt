package ru.sumenkov.savingscalendar

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sumenkov.savingscalendar.data.db.AppDatabase
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import ru.sumenkov.savingscalendar.data.update.ApkUpdateInstaller
import ru.sumenkov.savingscalendar.data.update.GitHubReleaseClient
import ru.sumenkov.savingscalendar.data.update.UpdateRepository
import ru.sumenkov.savingscalendar.notification.ReminderScheduler
import ru.sumenkov.savingscalendar.ui.NotificationPermissionUiState
import ru.sumenkov.savingscalendar.ui.SavingsApp
import ru.sumenkov.savingscalendar.ui.SavingsViewModel
import ru.sumenkov.savingscalendar.ui.SavingsViewModelFactory
import ru.sumenkov.savingscalendar.ui.theme.SavingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var permissionState by remember { mutableStateOf(readNotificationPermissionState()) }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { permissionState = readNotificationPermissionState() }
            )
            val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { permissionState = readNotificationPermissionState() }
            )

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        permissionState = readNotificationPermissionState()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val database = remember { AppDatabase.get(applicationContext) }
            val savingsRepository = remember { SavingsRepository(database.savingsDao()) }
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val reminderScheduler = remember { ReminderScheduler(applicationContext) }
            val updateRepository = remember {
                UpdateRepository(
                    releaseClient = GitHubReleaseClient(
                        owner = "sumenkov",
                        repository = "SavingsCalendar",
                        userAgent = "SavingsCalendar/${BuildConfig.VERSION_NAME}"
                    ),
                    currentVersionName = BuildConfig.VERSION_NAME
                )
            }
            val apkUpdateInstaller = remember { ApkUpdateInstaller(applicationContext) }
            var pendingInstallUri by remember { mutableStateOf<Uri?>(null) }
            val unknownSourcesSettingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {
                    pendingInstallUri?.let { uri ->
                        if (canInstallPackages()) {
                            pendingInstallUri = null
                            openApkInstaller(uri)
                        }
                    }
                }
            )

            var backgroundStartedAtMillis by remember { mutableStateOf<Long?>(null) }

            val viewModel: SavingsViewModel = viewModel(
                factory = SavingsViewModelFactory(
                    savingsRepository = savingsRepository,
                    settingsRepository = settingsRepository,
                    reminderScheduler = reminderScheduler,
                    updateRepository = updateRepository,
                    apkUpdateInstaller = apkUpdateInstaller
                )
            )

            DisposableEffect(lifecycleOwner, viewModel) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            viewModel.refreshToday()
                            backgroundStartedAtMillis?.let { startedAt ->
                                viewModel.checkForUpdatesAfterBackground(
                                    backgroundDurationMillis = System.currentTimeMillis() - startedAt
                                )
                                backgroundStartedAtMillis = null
                            }
                        }
                        Lifecycle.Event.ON_STOP -> {
                            backgroundStartedAtMillis = System.currentTimeMillis()
                        }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(permissionState.exactAlarmsGranted) {
                viewModel.syncNotificationSchedule()
            }

            SavingsTheme {
                SavingsApp(
                    viewModel = viewModel,
                    notificationPermissionState = permissionState,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenExactAlarmSettings = {
                        openExactAlarmSettings { intent ->
                            exactAlarmSettingsLauncher.launch(intent)
                        }
                    },
                    onInstallUpdate = { apkUri ->
                        if (canInstallPackages()) {
                            openApkInstaller(apkUri)
                        } else {
                            pendingInstallUri = apkUri
                            openUnknownSourcesSettings { intent ->
                                unknownSourcesSettingsLauncher.launch(intent)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun readNotificationPermissionState(): NotificationPermissionUiState {
        val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        val alarmManager = getSystemService(AlarmManager::class.java)
        val exactAlarmsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        return NotificationPermissionUiState(
            notificationsGranted = notificationsGranted,
            exactAlarmsGranted = exactAlarmsGranted,
            canRequestNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            canRequestExactAlarms = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
    }

    private fun openExactAlarmSettings(launch: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val packageUri = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = packageUri
        }
        try {
            launch(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = packageUri
                }
            )
        }
    }

    private fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            packageManager.canRequestPackageInstalls()
    }

    private fun openUnknownSourcesSettings(launch: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val packageUri = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = packageUri
        }
        try {
            launch(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = packageUri
                }
            )
        }
    }

    private fun openApkInstaller(apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}
