package com.denisshulika.fincentra.data.models.domain

import com.google.firebase.firestore.PropertyName

data class BankAccount(
    val id: String = "",
    val provider: String = "",
    val name: String = "",
    val type: String = "",
    val balance: Double = 0.0,
    val currencyCode: Int = 980,
    @get:PropertyName("selected")
    val selected: Boolean = false
)