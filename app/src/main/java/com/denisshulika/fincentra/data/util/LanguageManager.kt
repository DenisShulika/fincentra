package com.denisshulika.fincentra.data.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {

    fun initLocale() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()

        if (currentLocales.isEmpty) {
            val systemLang = Locale.getDefault().language
            val targetLang = if (systemLang == "uk") "uk" else "en"
            setLanguage(targetLang)
        }
    }

    fun setLanguage(langCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getCurrentLanguage(): String {
        return AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
    }
}