package com.bluepatitas.mobile.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.bluepatitas.mobile.domain.model.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLocaleController @Inject constructor() {
    fun apply(language: AppLanguage) {
        applyTag(language.tag)
    }

    fun currentLanguage(): AppLanguage {
        val activeTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return AppLanguage.fromTag(activeTag.ifBlank { AppLanguage.ENGLISH.tag })
    }

    companion object {
        fun applyTag(languageTag: String) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageTag)
            )
        }
    }
}
