package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.TransactionCategory

object MccDirectory {
    private val mccMap = mapOf(
        // --- ЇЖА (FOOD) ---
        5411 to TransactionCategory.FOOD, // Супермаркети
        5422 to TransactionCategory.FOOD, // М'ясні магазини
        5441 to TransactionCategory.FOOD, // Кондитерські
        5451 to TransactionCategory.FOOD, // Молочні продукти
        5462 to TransactionCategory.FOOD, // Булочні
        5499 to TransactionCategory.FOOD, // Продмаги
        5811 to TransactionCategory.FOOD, // Кейтеринг
        5812 to TransactionCategory.FOOD, // Ресторани
        5813 to TransactionCategory.FOOD, // Бари, нічні клуби
        5814 to TransactionCategory.FOOD, // Фастфуд

        // --- ТРАНСПОРТ (TRANSPORT) ---
        4111 to TransactionCategory.TRANSPORT, // Громадський транспорт
        4112 to TransactionCategory.TRANSPORT, // Залізниця
        4121 to TransactionCategory.TRANSPORT, // Таксі
        4131 to TransactionCategory.TRANSPORT, // Автобуси
        4511 to TransactionCategory.TRANSPORT, // Авіалінії
        4784 to TransactionCategory.TRANSPORT, // Платні дороги
        5511 to TransactionCategory.TRANSPORT, // Продаж авто
        5533 to TransactionCategory.TRANSPORT, // Автозапчастини
        5541 to TransactionCategory.TRANSPORT, // АЗС
        5542 to TransactionCategory.TRANSPORT, // АЗС самообслуговування
        7512 to TransactionCategory.TRANSPORT, // Оренда авто
        7523 to TransactionCategory.TRANSPORT, // Паркінги

        // --- ЖИТЛО ТА ПОБУТ (HOUSING) ---
        4900 to TransactionCategory.HOUSING, // Комунальні послуги
        4814 to TransactionCategory.HOUSING, // Оплата зв'язку (Мобільний)
        5211 to TransactionCategory.HOUSING, // Будівельні матеріали
        5261 to TransactionCategory.HOUSING, // Садові приналежності
        5712 to TransactionCategory.HOUSING, // Меблі
        5722 to TransactionCategory.HOUSING, // Побутова техніка

        // --- ЗДОРОВ'Я (HEALTH) ---
        5912 to TransactionCategory.HEALTH, // Аптеки
        5977 to TransactionCategory.HEALTH, // Косметика
        8011 to TransactionCategory.HEALTH, // Лікарі
        8021 to TransactionCategory.HEALTH, // Стоматологи
        8043 to TransactionCategory.HEALTH, // Оптика
        8062 to TransactionCategory.HEALTH, // Госпіталі
        8099 to TransactionCategory.HEALTH, // Мед. послуги інше

        // --- РОЗВАГИ (ENTERTAINMENT) ---
        5942 to TransactionCategory.ENTERTAINMENT, // Книжкові магазини
        7333 to TransactionCategory.ENTERTAINMENT, // Фотостудії
        7832 to TransactionCategory.ENTERTAINMENT, // Кінотеатри
        7922 to TransactionCategory.ENTERTAINMENT, // Театри
        7941 to TransactionCategory.ENTERTAINMENT, // Спортивні заходи
        7991 to TransactionCategory.ENTERTAINMENT, // Музеї
        7994 to TransactionCategory.ENTERTAINMENT, // Відеоігри
        7997 to TransactionCategory.ENTERTAINMENT, // Фітнес-центри
        7999 to TransactionCategory.ENTERTAINMENT, // Розваги інше

        // --- ЗАРПЛАТА (SALARY) ---
        6012 to TransactionCategory.SALARY, // Фінансові інститути (виплати)

        // --- ПОКУПКИ (можна віднести до OTHERS або створити нову) ---
        5311 to TransactionCategory.OTHERS, // Універмаги
        5621 to TransactionCategory.OTHERS, // Жіночий одяг
        5651 to TransactionCategory.OTHERS, // Сімейний одяг
        5661 to TransactionCategory.OTHERS, // Взуття
        5944 to TransactionCategory.OTHERS, // Ювелірні вироби
    )

    fun getCategory(mcc: Int?): TransactionCategory {
        return mccMap[mcc] ?: TransactionCategory.OTHERS
    }
}