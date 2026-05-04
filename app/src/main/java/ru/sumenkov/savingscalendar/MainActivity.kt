package ru.sumenkov.savingscalendar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sumenkov.savingscalendar.data.db.AppDatabase
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import ru.sumenkov.savingscalendar.notification.ReminderScheduler
import ru.sumenkov.savingscalendar.ui.SavingsApp
import ru.sumenkov.savingscalendar.ui.SavingsViewModel
import ru.sumenkov.savingscalendar.ui.SavingsViewModelFactory
import ru.sumenkov.savingscalendar.ui.theme.SavingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val database = remember { AppDatabase.get(applicationContext) }
            val savingsRepository = remember { SavingsRepository(database.savingsDao()) }
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val reminderScheduler = remember { ReminderScheduler(applicationContext) }

            val viewModel: SavingsViewModel = viewModel(
                factory = SavingsViewModelFactory(
                    savingsRepository = savingsRepository,
                    settingsRepository = settingsRepository,
                    reminderScheduler = reminderScheduler
                )
            )

            SavingsTheme {
                SavingsApp(viewModel = viewModel)
            }
        }
    }
}
