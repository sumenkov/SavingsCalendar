package ru.sumenkov.savingscalendar.data.update

data class AppUpdateInfo(
    val versionName: String,
    val title: String,
    val releaseUrl: String,
    val apkDownloadUrl: String,
    val apkName: String
)
