package com.denisshulika.fincentra.data.models.state

import com.denisshulika.fincentra.data.models.domain.TransactionCategory

data class CategoryStat(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float,
    val subCategories: List<SubCategoryStat>
)