package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.TransactionCategory

object MccDirectory {
    private val mccMap = mapOf(
        // --- AGRICULTURAL SERVICES (0001–1499) ---
        0742 to MccDetails(TransactionCategory.SERVICES, "Ветеринарні послуги"),
        0763 to MccDetails(TransactionCategory.SERVICES, "Агро-кооперативи"),
        0780 to MccDetails(TransactionCategory.SERVICES, "Ландшафтні послуги"),

        // --- CONTRACTED SERVICES (1500–2999) ---
        1520 to MccDetails(TransactionCategory.HOUSING, "Генпідрядники"),
        1711 to MccDetails(TransactionCategory.HOUSING, "Опалення та сантехніка"),
        1731 to MccDetails(TransactionCategory.HOUSING, "Електромонтаж"),
        1740 to MccDetails(TransactionCategory.HOUSING, "Будівельні роботи"),
        1799 to MccDetails(TransactionCategory.SERVICES, "Спец-підрядники"),
        2741 to MccDetails(TransactionCategory.SERVICES, "Друкарські послуги"),
        2842 to MccDetails(TransactionCategory.SERVICES, "Клінінг"),

        // --- TRANSPORTATION (4000–4799) ---
        4011 to MccDetails(TransactionCategory.TRAVEL, "Залізниця (Вантажі)"),
        4111 to MccDetails(TransactionCategory.TRANSPORT, "Громадський транспорт"),
        4112 to MccDetails(TransactionCategory.TRANSPORT, "Пасажирська залізниця"),
        4121 to MccDetails(TransactionCategory.TRANSPORT, "Таксі та лімузини"),
        4131 to MccDetails(TransactionCategory.TRANSPORT, "Автобусні лінії"),
        4215 to MccDetails(TransactionCategory.SERVICES, "Кур'єрські послуги"),
        4411 to MccDetails(TransactionCategory.TRAVEL, "Круїзні лінії"),
        4511 to MccDetails(TransactionCategory.TRAVEL, "Авіалінії"),
        4582 to MccDetails(TransactionCategory.TRAVEL, "Аеропорти"),
        4722 to MccDetails(TransactionCategory.TRAVEL, "Турагенції"),
        4784 to MccDetails(TransactionCategory.TRANSPORT, "Дорожні збори/Мости"),

        // --- UTILITIES (4800–4999) ---
        4812 to MccDetails(TransactionCategory.SHOPPING, "Обладнання зв'язку"),
        4814 to MccDetails(TransactionCategory.HOUSING, "Мобільний зв'язок"),
        4816 to MccDetails(TransactionCategory.SERVICES, "IT послуги"),
        4829 to MccDetails(TransactionCategory.TRANSFERS, "Грошові перекази"),
        4899 to MccDetails(TransactionCategory.SUBSCRIPTIONS, "ТБ/Радіо сервіси"),
        4900 to MccDetails(TransactionCategory.HOUSING, "Комунальні послуги"),

        // --- RETAIL OUTLET (5000–5599) ---
        5013 to MccDetails(TransactionCategory.TRANSPORT, "Автотовари"),
        5045 to MccDetails(TransactionCategory.SHOPPING, "Комп'ютери та ПЗ"),
        5094 to MccDetails(TransactionCategory.SHOPPING, "Ювелірні вироби"),
        5122 to MccDetails(TransactionCategory.HEALTH, "Аптечні товари"),
        5192 to MccDetails(TransactionCategory.SHOPPING, "Книги та газети"),
        5211 to MccDetails(TransactionCategory.HOUSING, "Будматеріали"),
        5261 to MccDetails(TransactionCategory.HOUSING, "Сад та город"),
        5310 to MccDetails(TransactionCategory.SHOPPING, "Дисконт-центри"),
        5311 to MccDetails(TransactionCategory.SHOPPING, "Універмаги"),
        5331 to MccDetails(TransactionCategory.SHOPPING, "Магазини низьких цін"),
        5411 to MccDetails(TransactionCategory.FOOD, "Супермаркети"),
        5441 to MccDetails(TransactionCategory.FOOD, "Кондитерські"),
        5462 to MccDetails(TransactionCategory.FOOD, "Булочні та пекарні"),
        5499 to MccDetails(TransactionCategory.FOOD, "Продукти харчування"),
        5511 to MccDetails(TransactionCategory.TRANSPORT, "Продаж авто"),
        5533 to MccDetails(TransactionCategory.TRANSPORT, "Автозапчастини"),
        5541 to MccDetails(TransactionCategory.TRANSPORT, "АЗС"),
        5542 to MccDetails(TransactionCategory.TRANSPORT, "АЗС (автомат)"),

        // --- CLOTHING (5600–5699) ---
        5611 to MccDetails(TransactionCategory.SHOPPING, "Чоловічий одяг"),
        5621 to MccDetails(TransactionCategory.SHOPPING, "Жіночий одяг"),
        5641 to MccDetails(TransactionCategory.SHOPPING, "Дитячий одяг"),
        5651 to MccDetails(TransactionCategory.SHOPPING, "Сімейний одяг"),
        5661 to MccDetails(TransactionCategory.SHOPPING, "Взуття"),

        // --- MISC STORES & EATING (5700–7299) ---
        5712 to MccDetails(TransactionCategory.HOUSING, "Меблі"),
        5722 to MccDetails(TransactionCategory.HOUSING, "Побутова техніка"),
        5732 to MccDetails(TransactionCategory.SHOPPING, "Електроніка"),
        5811 to MccDetails(TransactionCategory.FOOD, "Кейтеринг"),
        5812 to MccDetails(TransactionCategory.FOOD, "Ресторани"),
        5813 to MccDetails(TransactionCategory.FOOD, "Бари та клуби"),
        5814 to MccDetails(TransactionCategory.FOOD, "Фастфуд"),
        5912 to MccDetails(TransactionCategory.HEALTH, "Аптеки"),
        5921 to MccDetails(TransactionCategory.FOOD, "Алкогольні магазини"),
        5941 to MccDetails(TransactionCategory.ENTERTAINMENT, "Спорттовари"),
        5942 to MccDetails(TransactionCategory.ENTERTAINMENT, "Книжкові магазини"),
        5945 to MccDetails(TransactionCategory.ENTERTAINMENT, "Іграшки та хобі"),
        5977 to MccDetails(TransactionCategory.HEALTH, "Косметика"),
        5995 to MccDetails(TransactionCategory.SERVICES, "Зоотовари"),
        6010 to MccDetails(TransactionCategory.SALARY, "Готівка (каса)"),
        6011 to MccDetails(TransactionCategory.SALARY, "Готівка (банкомат)"),
        6538 to MccDetails(TransactionCategory.TRANSFERS, "Поповнення картки"),

        // --- SERVICES (7000–8999) ---
        7011 to MccDetails(TransactionCategory.TRAVEL, "Готелі та мотелі"),
        7230 to MccDetails(TransactionCategory.SERVICES, "Перукарні"),
        7298 to MccDetails(TransactionCategory.HEALTH, "SPA-салони"),
        7512 to MccDetails(TransactionCategory.TRANSPORT, "Оренда авто"),
        7542 to MccDetails(TransactionCategory.TRANSPORT, "Автомийки"),
        7832 to MccDetails(TransactionCategory.ENTERTAINMENT, "Кінотеатри"),
        7997 to MccDetails(TransactionCategory.ENTERTAINMENT, "Фітнес-клуби"),
        8011 to MccDetails(TransactionCategory.HEALTH, "Лікарі"),
        8021 to MccDetails(TransactionCategory.HEALTH, "Стоматологи"),
        8211 to MccDetails(TransactionCategory.EDUCATION, "Школи"),
        8220 to MccDetails(TransactionCategory.EDUCATION, "Університети"),
        8398 to MccDetails(TransactionCategory.GOVERNMENT, "Благодійність"),

        // --- GOVERNMENT (9000–9999) ---
        9222 to MccDetails(TransactionCategory.GOVERNMENT, "Штрафи"),
        9311 to MccDetails(TransactionCategory.GOVERNMENT, "Податки"),
        9399 to MccDetails(TransactionCategory.GOVERNMENT, "Держпослуги"),
        9402 to MccDetails(TransactionCategory.SERVICES, "Поштові послуги")
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