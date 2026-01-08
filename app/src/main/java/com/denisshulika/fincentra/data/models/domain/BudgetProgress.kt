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
            progress <= 0.5f -> R.drawable.img_tree_healthy
            progress <= 0.8f -> R.drawable.img_tree_medium
            progress < 1.0f -> R.drawable.img_tree_warning
            else -> R.drawable.img_tree_dead
        }

    val statusMessage: String
        get() = when {
            progress <= 0.5f -> "Все чудово!"
            progress <= 0.8f -> "Нормальний темп"
            progress < 1.0f -> "Бюджет закінчується"
            else -> "Межу перевищено"
        }
}