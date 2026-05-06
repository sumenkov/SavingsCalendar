package ru.sumenkov.savingscalendar.data.update

import ru.sumenkov.savingscalendar.domain.AppVersion

class UpdateRepository(
    private val releaseClient: GitHubReleaseSource,
    private val currentVersionName: String
) {
    suspend fun findAvailableUpdate(): AppUpdateInfo? {
        val release = releaseClient.latestRelease() ?: return null
        if (release.draft || release.prerelease) return null

        val remoteVersion = release.tagName.trim().removePrefix("v").removePrefix("V")
        val apkAsset = release.assets.firstOrNull { asset ->
            asset.name.endsWith(".apk", ignoreCase = true) &&
                !asset.name.contains("aligned", ignoreCase = true) &&
                asset.downloadUrl.isNotBlank()
        } ?: return null

        if (remoteVersion.isBlank()) return null
        if (release.releaseUrl.isBlank()) return null
        if (!AppVersion.isNewer(remoteVersion, currentVersionName)) return null

        return AppUpdateInfo(
            versionName = remoteVersion,
            title = release.title.ifBlank { "Версия $remoteVersion" },
            releaseUrl = release.releaseUrl,
            apkDownloadUrl = apkAsset.downloadUrl,
            apkName = apkAsset.name
        )
    }
}
