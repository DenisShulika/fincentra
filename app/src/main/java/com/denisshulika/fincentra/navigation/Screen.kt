package com.denisshulika.fincentra.navigation

import androidx.annotation.StringRes
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
import com.denisshulika.fincentra.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    data object Onboarding :
        Screen("onboarding", R.string.nav_onboarding, Icons.Default.AutoAwesome)

    data object Home : Screen("home", R.string.nav_home, Icons.Default.Dashboard)
    data object Transactions :
        Screen("transactions", R.string.nav_transactions, Icons.AutoMirrored.Filled.ReceiptLong)

    data object Stats : Screen("stats", R.string.nav_stats, Icons.Default.BarChart)
    data object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    data object Integrations :
        Screen("integrations", R.string.nav_integrations, Icons.Default.AccountBalance)

    data object Budgets : Screen("budgets_manage", R.string.nav_budgets, Icons.Default.TrackChanges)
    data object Dream : Screen("dream", R.string.nav_dream, Icons.Default.AutoAwesome)
    data object Login : Screen("login", R.string.nav_login, Icons.AutoMirrored.Filled.Login)
    data object Register : Screen("register", R.string.nav_register, Icons.Default.AppRegistration)
}