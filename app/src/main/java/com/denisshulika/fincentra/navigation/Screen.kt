package com.denisshulika.fincentra.navigation

sealed class Screen(val route: String) {
    object Transactions : Screen("transactions")
    object Integrations : Screen("integrations")
    object Profile : Screen("profile")
}