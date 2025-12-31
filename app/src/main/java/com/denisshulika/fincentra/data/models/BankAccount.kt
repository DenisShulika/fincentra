package com.denisshulika.fincentra.data.models

data class BankAccount(
    val id: String = "",
    val provider: String = "",
    val name: String = "",
    val type: String = "",
    val balance: Double = 0.0,
    val currencyCode: Int = 980,
    val isSelected: Boolean = true
)