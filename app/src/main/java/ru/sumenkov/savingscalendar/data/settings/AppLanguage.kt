package ru.sumenkov.savingscalendar.data.settings

import java.util.Locale

enum class AppLanguage {
    SYSTEM,
    EN,
    RU;

    fun resolve(systemLocale: Locale = Locale.getDefault()): AppLanguage {
        return when (this) {
            SYSTEM -> if (systemLocale.language == "ru") RU else EN
            EN -> EN
            RU -> RU
        }
    }

    fun locale(systemLocale: Locale = Locale.getDefault()): Locale {
        return when (resolve(systemLocale)) {
            SYSTEM -> systemLocale
            EN -> Locale.ENGLISH
            RU -> Locale("ru")
        }
    }
}
