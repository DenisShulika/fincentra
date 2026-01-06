package com.denisshulika.fincentra.data.models.domain

data class BudgetProgress(
    val budget: Budget,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Float
)