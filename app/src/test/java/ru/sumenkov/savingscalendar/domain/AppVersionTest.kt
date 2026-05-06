package ru.sumenkov.savingscalendar.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun detectsNewerPatchVersion() {
        assertTrue(AppVersion.isNewer(remoteVersion = "1.0.7", currentVersion = "1.0.6"))
    }

    @Test
    fun supportsTagPrefix() {
        assertTrue(AppVersion.isNewer(remoteVersion = "v1.1.0", currentVersion = "1.0.6"))
    }

    @Test
    fun comparesNumericPartsInsteadOfText() {
        assertTrue(AppVersion.isNewer(remoteVersion = "1.0.10", currentVersion = "1.0.9"))
    }

    @Test
    fun ignoresEqualVersionWithSuffix() {
        assertFalse(AppVersion.isNewer(remoteVersion = "1.0.6-release", currentVersion = "1.0.6"))
    }

    @Test
    fun ignoresOlderVersion() {
        assertFalse(AppVersion.isNewer(remoteVersion = "1.0.5", currentVersion = "1.0.6"))
    }
}
