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
import com.denisshulika.fincentra.ui.screens.RegisterScreen
import com.denisshulika.fincentra.ui.screens.StatsScreen
import com.denisshulika.fincentra.ui.screens.TransactionsScreen
import com.denisshulika.fincentra.ui.theme.FinCentraTheme
import com.denisshulika.fincentra.viewmodels.AuthViewModel
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel
import com.denisshulika.fincentra.viewmodels.ProfileViewModel
import com.denisshulika.fincentra.viewmodels.StatsViewModel
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

    val integrationsViewModel: IntegrationsViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()
    val budgetsViewModel: BudgetsViewModel = viewModel()

    val startDestination = remember {
        if (authRepository.getCurrentUser() != null) {
            Screen.Transactions.route
        } else {
            Screen.Login.route
        }
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
                        label = {
                            Text(text = Screen.Transactions.title)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        label = {
                            Text(text = Screen.Stats.title)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            navController.navigate(Screen.Stats.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Integrations.route,
                        label = {
                            Text(text = Screen.Integrations.title)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            navController.navigate(Screen.Integrations.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        label = {
                            Text(text = Screen.Profile.title)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
            composable(route = Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToMain = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToMain = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.Transactions.route) {
                TransactionsScreen(viewModel = transactionsViewModel)
            }

            composable(route = Screen.Stats.route) {
                StatsScreen(viewModel = statsViewModel)
            }

            composable(route = Screen.Integrations.route) {
                IntegrationsScreen(viewModel = integrationsViewModel)
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    budgetsViewModel = budgetsViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}