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

    val statusRes: Int
        get() = when {
            progress <= 0.2f -> R.string.budget_progress_status_wealthy
            progress <= 0.5f -> R.string.budget_progress_status_blooming
            progress <= 0.8f -> R.string.budget_progress_status_healthy
            progress < 1.0f -> R.string.budget_progress_status_needs_care
            else -> R.string.budget_progress_status_withered
        }
}