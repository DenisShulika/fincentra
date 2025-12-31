package com.denisshulika.fincentra.data.models

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String = "manual",
    val amount: Double = 0.0,
    val currencyCode: Int = 980,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: TransactionCategory = TransactionCategory.OTHERS,
    val bankName: String = "Готівка",

    @get:PropertyName("isExpense")
    @set:PropertyName("isExpense")
    var isExpense: Boolean = true,

    val mcc: Int? = null,
    val note: String? = null
)