package com.denisshulika.fincentra.data.models

enum class StatsPeriod(val displayName: String) {
    WEEK("Тиждень"),
    MONTH("Місяць"),
    QUARTER("3 місяці"),
    ALL("Весь час"),
    CUSTOM("Календар")
}