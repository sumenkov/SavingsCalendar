package ru.sumenkov.savingscalendar.data.update

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateRepositoryTest {
    @Test
    fun returnsUpdateWhenReleaseHasNewerApkAsset() = runBlocking {
        val repository = UpdateRepository(
            releaseClient = FakeReleaseSource(
                GitHubRelease(
                    tagName = "v1.0.7",
                    title = "Версия 1.0.7",
                    releaseUrl = "https://github.com/sumenkov/SavingsCalendar/releases/tag/v1.0.7",
                    draft = false,
                    prerelease = false,
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "savings-calendar-1.0.7.apk",
                            downloadUrl = "https://github.com/sumenkov/SavingsCalendar/releases/download/v1.0.7/savings-calendar-1.0.7.apk"
                        )
                    )
                )
            ),
            currentVersionName = "1.0.6"
        )

        val update = repository.findAvailableUpdate()

        assertEquals("1.0.7", update?.versionName)
        assertEquals("savings-calendar-1.0.7.apk", update?.apkName)
    }

    @Test
    fun ignoresReleaseWithoutInstallableApkAsset() = runBlocking {
        val repository = UpdateRepository(
            releaseClient = FakeReleaseSource(
                GitHubRelease(
                    tagName = "v1.0.7",
                    title = "Версия 1.0.7",
                    releaseUrl = "https://github.com/sumenkov/SavingsCalendar/releases/tag/v1.0.7",
                    draft = false,
                    prerelease = false,
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "savings-calendar-1.0.7-aligned.apk",
                            downloadUrl = "https://example.com/aligned.apk"
                        )
                    )
                )
            ),
            currentVersionName = "1.0.6"
        )

        assertNull(repository.findAvailableUpdate())
    }

    @Test
    fun ignoresPrerelease() = runBlocking {
        val repository = UpdateRepository(
            releaseClient = FakeReleaseSource(
                GitHubRelease(
                    tagName = "v1.0.7",
                    title = "Версия 1.0.7",
                    releaseUrl = "https://github.com/sumenkov/SavingsCalendar/releases/tag/v1.0.7",
                    draft = false,
                    prerelease = true,
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "savings-calendar-1.0.7.apk",
                            downloadUrl = "https://example.com/app.apk"
                        )
                    )
                )
            ),
            currentVersionName = "1.0.6"
        )

        assertNull(repository.findAvailableUpdate())
    }

    private class FakeReleaseSource(
        private val release: GitHubRelease?
    ) : GitHubReleaseSource {
        override suspend fun latestRelease(): GitHubRelease? = release
    }
}
