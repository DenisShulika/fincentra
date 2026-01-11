package com.denisshulika.fincentra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.data.util.PrefConstants
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.screens.BudgetsScreen
import com.denisshulika.fincentra.ui.screens.DreamScreen
import com.denisshulika.fincentra.ui.screens.HomeScreen
import com.denisshulika.fincentra.ui.screens.IntegrationsScreen
import com.denisshulika.fincentra.ui.screens.LoginScreen
import com.denisshulika.fincentra.ui.screens.OnboardingScreen
import com.denisshulika.fincentra.ui.screens.ProfileScreen
import com.denisshulika.fincentra.ui.screens.RegisterScreen
import com.denisshulika.fincentra.ui.screens.StatsScreen
import com.denisshulika.fincentra.ui.screens.TransactionsScreen
import com.denisshulika.fincentra.ui.theme.FinCentraTheme
import com.denisshulika.fincentra.viewmodels.AuthViewModel
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.DreamViewModel
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel
import com.denisshulika.fincentra.viewmodels.ProfileViewModel
import com.denisshulika.fincentra.viewmodels.StatsViewModel
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val prefs =
        remember { context.getSharedPreferences(PrefConstants.PREFS_NAME, Context.MODE_PRIVATE) }
    val isOnboardingCompleted =
        remember { prefs.getBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, false) }

    val startDestination = remember {
        when {
            !isOnboardingCompleted -> Screen.Onboarding.route
            DependencyProvider.authRepository.getCurrentUser() != null -> Screen.Home.route
            else -> Screen.Login.route
        }
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = viewModel()
    val integrationsViewModel: IntegrationsViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val budgetsViewModel: BudgetsViewModel = viewModel()
    val dreamViewModel: DreamViewModel = viewModel()

    val navigateWithClearStack: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isAuthScreen = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
    val isTopLevelScreen = currentRoute in listOf(
        Screen.Home.route,
        Screen.Transactions.route,
        Screen.Stats.route
    )

    val showNavElements = currentRoute !in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.Onboarding.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showNavElements && isTopLevelScreen,
        drawerContent = {
            if (showNavElements) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "FinCentra",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()

                    val drawerItems = listOf(
                        Screen.Profile,
                        Screen.Integrations,
                        Screen.Budgets,
                        Screen.Dream
                    )

                    drawerItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, null) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showNavElements) {
                    NavigationBar {
                        val bottomItems = listOf(Screen.Home, Screen.Transactions, Screen.Stats)
                        bottomItems.forEach { screen ->
                            NavigationBarItem(
                                selected = currentRoute == screen.route,
                                label = { Text(screen.title) },
                                icon = { Icon(screen.icon, null) },
                                onClick = { navigateWithClearStack(screen.route) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            prefs.edit().putBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, true)
                                .apply()
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        statsViewModel = statsViewModel,
                        transactionsViewModel = transactionsViewModel,
                        budgetsViewModel = budgetsViewModel,
                        dreamViewModel = dreamViewModel,
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) }
                    )
                }

                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        viewModel = transactionsViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(
                        viewModel = statsViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                composable(Screen.Integrations.route) {
                    IntegrationsScreen(
                        viewModel = integrationsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Budgets.route) {
                    BudgetsScreen(
                        viewModel = budgetsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Dream.route) {
                    DreamScreen(
                        viewModel = dreamViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onBack = { navController.popBackStack() },
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
}
