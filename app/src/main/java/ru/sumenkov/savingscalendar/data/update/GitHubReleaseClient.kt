package ru.sumenkov.savingscalendar.data.update

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface GitHubReleaseSource {
    suspend fun latestRelease(): GitHubRelease?
}

class GitHubReleaseClient(
    private val owner: String,
    private val repository: String,
    private val userAgent: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : GitHubReleaseSource {
    override suspend fun latestRelease(): GitHubRelease? = withContext(dispatcher) {
        val connection = (URL("https://api.github.com/repos/$owner/$repository/releases/latest").openConnection() as HttpURLConnection)
            .apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MILLIS
                readTimeout = READ_TIMEOUT_MILLIS
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", userAgent)
            }

        try {
            when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> connection.inputStream.bufferedReader().use { reader ->
                    val json = JSONObject(reader.readText())
                    GitHubRelease(
                        tagName = json.optString("tag_name"),
                        title = json.optString("name"),
                        releaseUrl = json.optString("html_url"),
                        draft = json.optBoolean("draft"),
                        prerelease = json.optBoolean("prerelease"),
                        assets = json.optJSONArray("assets").toReleaseAssets()
                    )
                }
                HttpURLConnection.HTTP_NOT_FOUND -> null
                else -> throw IOException("GitHub release request failed: HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 10_000
        const val READ_TIMEOUT_MILLIS = 10_000
    }
}

data class GitHubRelease(
    val tagName: String,
    val title: String,
    val releaseUrl: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<GitHubReleaseAsset>
)

data class GitHubReleaseAsset(
    val name: String,
    val downloadUrl: String
)

private fun JSONArray?.toReleaseAssets(): List<GitHubReleaseAsset> {
    if (this == null) return emptyList()

    return buildList {
        for (index in 0 until length()) {
            val asset = optJSONObject(index) ?: continue
            add(
                GitHubReleaseAsset(
                    name = asset.optString("name"),
                    downloadUrl = asset.optString("browser_download_url")
                )
            )
        }
    }
}
