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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector
import com.denisshulika.fincentra.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    data object Onboarding :
        Screen("onboarding", R.string.screen_nav_onboarding, Icons.Default.AutoAwesome)

    data object Home : Screen("home", R.string.screen_nav_home, Icons.Default.Dashboard)
    data object Transactions : Screen(
        "transactions",
        R.string.screen_nav_transactions,
        Icons.AutoMirrored.Filled.ReceiptLong
    )

    data object Stats : Screen("stats", R.string.screen_nav_stats, Icons.Default.BarChart)
    data object Profile : Screen("profile", R.string.screen_nav_profile, Icons.Default.Person)
    data object Integrations :
        Screen("integrations", R.string.screen_nav_integrations, Icons.Default.AccountBalance)

    data object Budgets :
        Screen("budgets_manage", R.string.screen_nav_budgets, Icons.Default.TrackChanges)

    data object Dream : Screen("dream", R.string.screen_nav_dream, Icons.Default.AutoAwesome)
    data object About : Screen("about", R.string.screen_nav_about, Icons.Default.Info)
    data object Settings : Screen("settings", R.string.screen_nav_settings, Icons.Default.Settings)
    data object Login : Screen("login", R.string.screen_nav_login, Icons.AutoMirrored.Filled.Login)
    data object Register :
        Screen("register", R.string.screen_nav_register, Icons.Default.AppRegistration)

    data object Subscriptions : Screen(
        route = "subscriptions",
        titleRes = R.string.screen_nav_subscriptions,
        icon = Icons.Default.Repeat
    )

    data object Export : Screen(
        route = "export",
        titleRes = R.string.screen_nav_export,
        icon = Icons.Default.Description
    )
}