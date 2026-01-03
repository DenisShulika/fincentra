package com.denisshulika.fincentra.navigation

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Вхід")
    data object Register : Screen("register", "Реєстрація")
    data object Transactions : Screen("transactions", "Транзакції")
    data object Stats : Screen("stats", "Статистика")
    data object Integrations : Screen("integrations", "Банки")
    data object Profile : Screen("profile", "Профіль")
}