package com.denisshulika.fincentra.data.models.state

import androidx.annotation.StringRes
import com.denisshulika.fincentra.R

enum class StatsPeriod(@StringRes val displayNameRes: Int) {
    WEEK(R.string.stats_period_week),
    MONTH(R.string.stats_period_month),
    QUARTER(R.string.stats_period_quarter),
    ALL(R.string.stats_period_all),
    CUSTOM(R.string.stats_period_custom)
}