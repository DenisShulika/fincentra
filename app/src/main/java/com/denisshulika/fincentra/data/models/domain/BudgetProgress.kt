package com.denisshulika.fincentra.data.models.domain

import com.denisshulika.fincentra.R

data class BudgetProgress(
    val budget: Budget,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Float
) {
    val treeImageRes: Int
        get() = when {
            progress <= 0.2f -> R.drawable.img_tree_money
            progress <= 0.5f -> R.drawable.img_tree_bloom
            progress <= 0.8f -> R.drawable.img_tree_healthy
            progress < 1.0f -> R.drawable.img_tree_wither
            else -> R.drawable.img_tree_dead
        }

    val statusMessage: String
        get() = when {
            progress <= 0.2f -> "Wealthy"
            progress <= 0.5f -> "Blooming"
            progress <= 0.8f -> "Healthy"
            progress < 1.0f -> "Needs care"
            else -> "Withered"
        }
}