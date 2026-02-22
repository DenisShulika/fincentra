package com.denisshulika.fincentra.data.models.state

import androidx.annotation.StringRes
import com.denisshulika.fincentra.R

enum class StatsPeriod(@StringRes val displayNameRes: Int) {
    WEEK(R.string.period_week),
    MONTH(R.string.period_month),
    QUARTER(R.string.period_quarter),
    ALL(R.string.period_all),
    CUSTOM(R.string.period_custom)
}