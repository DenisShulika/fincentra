package com.denisshulika.fincentra.data.models

data class CategoryStat(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float,
    val subCategories: List<SubCategoryStat>
)