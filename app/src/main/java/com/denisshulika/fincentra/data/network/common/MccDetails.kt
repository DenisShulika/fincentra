package com.denisshulika.fincentra.data.network.common

import androidx.annotation.StringRes
import com.denisshulika.fincentra.data.models.domain.TransactionCategory

data class MccDetails(
    val category: TransactionCategory,
    @StringRes val subCategoryRes: Int
)