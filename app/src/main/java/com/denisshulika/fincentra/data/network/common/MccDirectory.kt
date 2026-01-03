package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.TransactionCategory

object MccDirectory {
    private val mccMap = mapOf(
        // --- ЇЖА (FOOD) ---
        5411 to MccDetails(TransactionCategory.FOOD, "Супермаркети"),
        5422 to MccDetails(TransactionCategory.FOOD, "М'ясні магазини"),
        5441 to MccDetails(TransactionCategory.FOOD, "Кондитерські"),
        5451 to MccDetails(TransactionCategory.FOOD, "Молочні продукти"),
        5462 to MccDetails(TransactionCategory.FOOD, "Булочні та пекарні"),
        5499 to MccDetails(TransactionCategory.FOOD, "Продуктові магазини"),
        5811 to MccDetails(TransactionCategory.FOOD, "Кейтеринг"),
        5812 to MccDetails(TransactionCategory.FOOD, "Ресторани та кафе"),
        5813 to MccDetails(TransactionCategory.FOOD, "Бари та нічні клуби"),
        5814 to MccDetails(TransactionCategory.FOOD, "Фастфуд"),

        // --- ТРАНСПОРТ ТА АВТО (TRANSPORT) ---
        4111 to MccDetails(TransactionCategory.TRANSPORT, "Громадський транспорт"),
        4112 to MccDetails(TransactionCategory.TRANSPORT, "Залізниця"),
        4121 to MccDetails(TransactionCategory.TRANSPORT, "Таксі"),
        4131 to MccDetails(TransactionCategory.TRANSPORT, "Автобусні лінії"),
        4511 to MccDetails(TransactionCategory.TRANSPORT, "Авіалінії"),
        4784 to MccDetails(TransactionCategory.TRANSPORT, "Платні дороги"),
        5511 to MccDetails(TransactionCategory.TRANSPORT, "Продаж авто"),
        5533 to MccDetails(TransactionCategory.TRANSPORT, "Автозапчастини"),
        5541 to MccDetails(TransactionCategory.TRANSPORT, "АЗС"),
        5542 to MccDetails(TransactionCategory.TRANSPORT, "АЗС самообслуговування"),
        7512 to MccDetails(TransactionCategory.TRANSPORT, "Прокат авто"),
        7523 to MccDetails(TransactionCategory.TRANSPORT, "Паркінги"),

        // --- ЖИТЛО ТА ПОБУТ (HOUSING) ---
        4812 to MccDetails(TransactionCategory.HOUSING, "Продаж телефонів"),
        4814 to MccDetails(TransactionCategory.HOUSING, "Мобільний зв'язок"),
        4900 to MccDetails(TransactionCategory.HOUSING, "Комунальні послуги"),
        5211 to MccDetails(TransactionCategory.HOUSING, "Будівельні матеріали"),
        5261 to MccDetails(TransactionCategory.HOUSING, "Садові товари"),
        5712 to MccDetails(TransactionCategory.HOUSING, "Меблі"),
        5722 to MccDetails(TransactionCategory.HOUSING, "Побутова техніка"),
        5732 to MccDetails(TransactionCategory.HOUSING, "Електроніка"),

        // --- ЗДОРОВ'Я (HEALTH) ---
        5912 to MccDetails(TransactionCategory.HEALTH, "Аптеки"),
        5977 to MccDetails(TransactionCategory.HEALTH, "Косметика"),
        8011 to MccDetails(TransactionCategory.HEALTH, "Лікарі та клініки"),
        8021 to MccDetails(TransactionCategory.HEALTH, "Стоматологія"),
        8043 to MccDetails(TransactionCategory.HEALTH, "Оптика"),
        8062 to MccDetails(TransactionCategory.HEALTH, "Госпіталі"),
        8099 to MccDetails(TransactionCategory.HEALTH, "Медичні послуги"),

        // --- РОЗВАГИ (ENTERTAINMENT) ---
        5942 to MccDetails(TransactionCategory.ENTERTAINMENT, "Книжкові магазини"),
        7333 to MccDetails(TransactionCategory.ENTERTAINMENT, "Фотостудії"),
        7832 to MccDetails(TransactionCategory.ENTERTAINMENT, "Кінотеатри"),
        7922 to MccDetails(TransactionCategory.ENTERTAINMENT, "Театри та концерти"),
        7941 to MccDetails(TransactionCategory.ENTERTAINMENT, "Спортивні заходи"),
        7991 to MccDetails(TransactionCategory.ENTERTAINMENT, "Музеї"),
        7994 to MccDetails(TransactionCategory.ENTERTAINMENT, "Відеоігри"),
        7997 to MccDetails(TransactionCategory.ENTERTAINMENT, "Фітнес-центри"),
        7999 to MccDetails(TransactionCategory.ENTERTAINMENT, "Розваги інше"),

        // --- ПІДПИСКИ ТА СЕРВІСИ (SUBSCRIPTIONS) ---
        4899 to MccDetails(TransactionCategory.SUBSCRIPTIONS, "Кабельне/Цифрове ТБ"),
        5815 to MccDetails(TransactionCategory.SUBSCRIPTIONS, "Цифровий контент"),
        5817 to MccDetails(TransactionCategory.SUBSCRIPTIONS, "Цифрові програми/ігри"),

        // --- ЗАРПЛАТА ТА ФІНАНСИ (SALARY) ---
        6010 to MccDetails(TransactionCategory.SALARY, "Зняття готівки (банк)"),
        6011 to MccDetails(TransactionCategory.SALARY, "Зняття готівки (банкомат)"),
        6012 to MccDetails(TransactionCategory.SALARY, "Фінансові послуги"),
        6538 to MccDetails(TransactionCategory.SALARY, "Грошові перекази"),

        // --- ПОКУПКИ ТА ІНШЕ (OTHERS) ---
        5311 to MccDetails(TransactionCategory.OTHERS, "Універмаги"),
        5331 to MccDetails(TransactionCategory.OTHERS, "Магазини низьких цін"),
        5611 to MccDetails(TransactionCategory.OTHERS, "Чоловічий одяг"),
        5621 to MccDetails(TransactionCategory.OTHERS, "Жіночий одяг"),
        5651 to MccDetails(TransactionCategory.OTHERS, "Сімейний одяг"),
        5661 to MccDetails(TransactionCategory.OTHERS, "Взуття"),
        5691 to MccDetails(TransactionCategory.OTHERS, "Магазини одягу"),
        5941 to MccDetails(TransactionCategory.OTHERS, "Спортивні товари"),
        5944 to MccDetails(TransactionCategory.OTHERS, "Ювелірні вироби"),
        5945 to MccDetails(TransactionCategory.OTHERS, "Іграшки та хобі"),
        7230 to MccDetails(TransactionCategory.OTHERS, "Салони краси"),
        7298 to MccDetails(TransactionCategory.OTHERS, "SPA послуги")
    )

    fun getSubcategoriesFor(category: TransactionCategory): List<String> {
        return mccMap.values
            .filter { it.category == category }
            .map { it.subCategoryName }
            .distinct()
            .sorted()
    }

    fun getCategory(mcc: Int?): TransactionCategory {
        return mccMap[mcc]?.category ?: TransactionCategory.OTHERS
    }

    fun getDetails(mcc: Int?): MccDetails {
        return mccMap[mcc] ?: MccDetails(TransactionCategory.OTHERS, "Різне")
    }
}