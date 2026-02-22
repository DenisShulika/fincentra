package com.denisshulika.fincentra.data.models.state

import androidx.annotation.StringRes

data class SubCategoryStat(
    @StringRes val nameRes: Int,
    val amount: Double,
    val percentageOfParent: Float
)