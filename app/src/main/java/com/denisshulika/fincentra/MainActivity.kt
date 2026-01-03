package com.denisshulika.fincentra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.screens.IntegrationsScreen
import com.denisshulika.fincentra.ui.screens.LoginScreen
import com.denisshulika.fincentra.ui.screens.ProfileScreen
import com.denisshulika.fincentra.ui.screens.StatsScreen
import com.denisshulika.fincentra.ui.screens.TransactionsScreen
import com.denisshulika.fincentra.ui.theme.FinCentraTheme
import com.denisshulika.fincentra.viewmodels.AuthViewModel
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel
import com.denisshulika.fincentra.viewmodels.ProfileViewModel
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinCentraTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authRepository = DependencyProvider.authRepository

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = remember {
        if (authRepository.getCurrentUser() != null) Screen.Transactions.route else Screen.Login.route
    }

    val screensWithBottomBar = listOf(
        Screen.Transactions.route,
        Screen.Stats.route,
        Screen.Integrations.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Transactions.route,
                        onClick = { navController.navigate(Screen.Transactions.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }},
                        label = { Text(Screen.Transactions.title) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        onClick = { navController.navigate(Screen.Stats.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }},
                        label = { Text(Screen.Stats.title) },
                        icon = { Icon(Icons.Default.BarChart, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Integrations.route,
                        onClick = { navController.navigate(Screen.Integrations.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }},
                        label = { Text(Screen.Integrations.title) },
                        icon = { Icon(Icons.Default.AccountBalance, null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = { navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }},
                        label = { Text(Screen.Profile.title) },
                        icon = { Icon(Icons.Default.Person, null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = viewModel()
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToMain = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { /* navController.navigate(Screen.Register.route) */ }
                )
            }

            composable(Screen.Transactions.route) {
                val viewModel: TransactionsViewModel = viewModel()
                TransactionsScreen(viewModel)
            }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Integrations.route) {
                val viewModel: IntegrationsViewModel = viewModel()
                IntegrationsScreen(viewModel)
            }
            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = viewModel()
                ProfileScreen(viewModel)
            }
        }
    }
}