package com.denisshulika.fincentra.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", "Вступ", Icons.Default.AutoAwesome)
    data object Home : Screen("home", "Огляд", Icons.Default.Dashboard)
    data object Transactions :
        Screen("transactions", "Транзакції", Icons.AutoMirrored.Filled.ReceiptLong)

    data object Stats : Screen("stats", "Статистика", Icons.Default.BarChart)
    data object Profile : Screen("profile", "Профіль", Icons.Default.Person)
    data object Integrations : Screen("integrations", "Банки", Icons.Default.AccountBalance)
    data object Budgets : Screen("budgets_manage", "Ліміти", Icons.Default.TrackChanges)
    data object Dream : Screen("dream", "Мрія", Icons.Default.AutoAwesome)
    data object Login : Screen("login", "Вхід", Icons.AutoMirrored.Filled.Login)
    data object Register : Screen("register", "Реєстрація", Icons.Default.AppRegistration)
}