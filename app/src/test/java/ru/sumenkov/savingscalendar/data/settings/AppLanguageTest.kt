package ru.sumenkov.savingscalendar.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {
    @Test
    fun systemLanguageUsesRussianForRussianLocale() {
        assertEquals(AppLanguage.RU, AppLanguage.SYSTEM.resolve(Locale("ru")))
    }

    @Test
    fun systemLanguageUsesEnglishForOtherLocales() {
        assertEquals(AppLanguage.EN, AppLanguage.SYSTEM.resolve(Locale.ENGLISH))
        assertEquals(AppLanguage.EN, AppLanguage.SYSTEM.resolve(Locale.GERMAN))
    }

    @Test
    fun explicitLanguageIgnoresSystemLocale() {
        assertEquals(AppLanguage.EN, AppLanguage.EN.resolve(Locale("ru")))
        assertEquals(AppLanguage.RU, AppLanguage.RU.resolve(Locale.ENGLISH))
    }
}
