package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.domain.TransactionCategory

data class MccDetails(
    val category: TransactionCategory,
    val subCategoryName: String
)