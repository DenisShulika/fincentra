package com.denisshulika.fincentra.data.models

enum class TransactionCategory(val displayName: String) {
    FOOD("Їжа"),
    TRANSPORT("Транспорт"),
    HOUSING("Житло"),
    HEALTH("Здоров'я"),
    ENTERTAINMENT("Розваги"),
    SALARY("Зарплата"),
    OTHERS("Різне")
}