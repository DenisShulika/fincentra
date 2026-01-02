package com.denisshulika.fincentra.data.network.common

object CurrencyMapper {
    fun getSymbol(code: Int): String {
        return when (code) {
            980 -> "₴"
            840 -> "$"
            978 -> "€"
            826 -> "£"
            203 -> "Kč"
            348 -> "Ft"
            949 -> "₺"
            756 -> "₣"
            4217 -> "zł"
            376 -> "₪"
            124 -> "C$"
            959 -> "AU"
            else -> "¤ ($code)"
        }
    }

    fun getCodeName(code: Int): String {
        return when (code) {
            980 -> "UAH"
            840 -> "USD"
            978 -> "EUR"
            826 -> "GBP"
            203 -> "CZK"
            348 -> "HUF"
            949 -> "TRY"
            756 -> "CHF"
            4217 -> "PLN"
            else -> "Code: $code"
        }
    }
}