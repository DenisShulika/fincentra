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
            progress <= 0.5f -> R.drawable.img_tree_money
            progress < 1.0f -> R.drawable.img_tree_healthy
            else -> R.drawable.img_tree_dead
        }

    val statusMessage: String
        get() = when {
            progress <= 0.5f -> "Запас безпечний"
            progress < 1.0f -> "Витрачаєте ліміт"
            else -> "Ліміт порушено!"
        }
}