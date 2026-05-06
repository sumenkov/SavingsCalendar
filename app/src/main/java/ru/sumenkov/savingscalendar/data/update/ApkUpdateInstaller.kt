package ru.sumenkov.savingscalendar.data.update

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ApkUpdateInstaller(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val appContext = context.applicationContext

    suspend fun download(update: AppUpdateInfo, onProgress: (Int?) -> Unit): Uri = withContext(dispatcher) {
        val updatesDir = File(appContext.cacheDir, UPDATES_DIR).apply { mkdirs() }
        val apkFile = File(updatesDir, update.apkName.safeApkFileName(update.versionName))
        val connection = (URL(update.apkDownloadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", "SavingsCalendar")
        }

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("APK download failed: HTTP ${connection.responseCode}")
            }

            val contentLength = connection.contentLengthLong.takeIf { it > 0L }
            var downloaded = 0L
            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress(contentLength?.let { total -> ((downloaded * 100) / total).toInt().coerceIn(0, 100) })
                    }
                }
            }
            onProgress(100)

            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                apkFile
            )
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val UPDATES_DIR = "updates"
        const val CONNECT_TIMEOUT_MILLIS = 15_000
        const val READ_TIMEOUT_MILLIS = 30_000
    }
}

private fun String.safeApkFileName(versionName: String): String {
    val fallback = "savings-calendar-$versionName.apk"
    val normalized = substringAfterLast('/')
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .takeIf { it.endsWith(".apk", ignoreCase = true) }
        ?: fallback

    return normalized.ifBlank { fallback }
}
