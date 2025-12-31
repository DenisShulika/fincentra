package com.denisshulika.fincentra.data.network.common

object CurrencyMapper {
    fun getSymbol(code: Int): String {
        return when (code) {
            980 -> "UAH"
            840 -> "USD"
            978 -> "EUR"
            959 -> "XAU"
            4217 -> "PLN"
            else -> "Code: $code"
        }
    }
}