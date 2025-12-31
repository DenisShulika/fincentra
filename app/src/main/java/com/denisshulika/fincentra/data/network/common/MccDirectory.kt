package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.TransactionCategory

object MccDirectory {
    private val mccMap = mapOf(
        // Їжа
        5411 to TransactionCategory.FOOD,    // Супермаркети
        5499 to TransactionCategory.FOOD,    // Продмаги
        5812 to TransactionCategory.FOOD,    // Ресторани
        5814 to TransactionCategory.FOOD,    // Фастфуд

        // Транспорт
        4121 to TransactionCategory.TRANSPORT, // Таксі
        5541 to TransactionCategory.TRANSPORT, // АЗС
        4111 to TransactionCategory.TRANSPORT, // Громадський транспорт

        // Розваги
        7997 to TransactionCategory.ENTERTAINMENT, // Фітнес-клуби
        5942 to TransactionCategory.ENTERTAINMENT, // Книжкові магазини

        // Здоров'я
        5912 to TransactionCategory.HEALTH,   // Аптеки
        8011 to TransactionCategory.HEALTH    // Лікарі
    )

    fun getCategory(mcc: Int?): TransactionCategory {
        return mccMap[mcc] ?: TransactionCategory.OTHERS
    }
}