package com.denisshulika.fincentra.data.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    private val supportedLanguages = listOf("uk", "en", "pl", "de")

    fun initLocale() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            val systemLang = Locale.getDefault().language
            val targetLang = if (systemLang in supportedLanguages) systemLang else "en"
            setLanguage(targetLang)
        }
    }

    fun setLanguage(langCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}