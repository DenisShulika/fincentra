package com.denisshulika.fincentra.data.models

import com.google.firebase.firestore.PropertyName

data class Transaction(
    val id: String = "",
    val accountId: String = "manual",
    val amount: Double = 0.0,
    val currencyCode: Int = 980,
    val description: String = "",
    val timestamp: Long = 0L,
    val category: TransactionCategory = TransactionCategory.OTHERS,
    val bankName: String = "Готівка",

    @get:PropertyName("isExpense")
    @set:PropertyName("isExpense")
    var isExpense: Boolean = true,

    val mcc: Int? = null,
    val note: String? = null,
    val balance: Double? = null,
    val comment: String? = null
)