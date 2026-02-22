package com.denisshulika.fincentra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.data.util.LanguageManager
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
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        LanguageManager.initLocale()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            val user = DependencyProvider.authRepository.getCurrentUser()
            if (user != null) {
                val repository = DependencyProvider.financeRepository
                !repository.isInitialLoadComplete.value
            } else {
                false
            }
        }

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
    val prefs = remember {
        context.getSharedPreferences(PrefConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    val isOnboardingCompleted = remember {
        prefs.getBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, false)
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = viewModel()
    val integrationsViewModel: IntegrationsViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val budgetsViewModel: BudgetsViewModel = viewModel()
    val dreamViewModel: DreamViewModel = viewModel()

    val currentUser = DependencyProvider.authRepository.getCurrentUser()
    val startDestination = remember(currentUser, isOnboardingCompleted) {
        when {
            !isOnboardingCompleted -> Screen.Onboarding.route
            currentUser != null -> Screen.Home.route
            else -> Screen.Login.route
        }
    }

    val isMainScreen =
        currentRoute in listOf(
            Screen.Login.route,
            Screen.Register.route,
            Screen.Onboarding.route,
            Screen.Profile.route,
            Screen.Integrations.route,
            Screen.Budgets.route,
            Screen.Dream.route
        )
    val isTopLevelScreen =
        currentRoute in listOf(Screen.Home.route, Screen.Transactions.route, Screen.Stats.route)

    LaunchedEffect(currentRoute) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.snapTo(DrawerValue.Closed) }
        }
    }

    val navigateWithClearStack: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevelScreen,
        drawerContent = {
            if (!isMainScreen) {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.background,
                    drawerTonalElevation = 0.dp
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "FinCentra",
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    val drawerItems =
                        listOf(Screen.Profile, Screen.Integrations, Screen.Budgets, Screen.Dream)
                    drawerItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, null) },
                            label = { Text(stringResource(screen.titleRes)) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.5f
                                ),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!isMainScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        val bottomItems = listOf(Screen.Home, Screen.Transactions, Screen.Stats)
                        bottomItems.forEach { screen ->
                            NavigationBarItem(
                                selected = currentRoute == screen.route,
                                label = {
                                    Text(
                                        text = stringResource(screen.titleRes),
                                        fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(screen.icon, null) },
                                onClick = { navigateWithClearStack(screen.route) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.6f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(
                    bottom = if (!isMainScreen) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen {
                        prefs.edit().putBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, true)
                            .apply()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        },
                        onBackToLogin = { navController.popBackStack() }
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
                    TransactionsScreen(transactionsViewModel) { scope.launch { drawerState.open() } }
                }
                composable(Screen.Stats.route) {
                    StatsScreen(statsViewModel) { scope.launch { drawerState.open() } }
                }
                composable(Screen.Integrations.route) {
                    IntegrationsScreen(integrationsViewModel) { navController.popBackStack() }
                }
                composable(Screen.Budgets.route) {
                    BudgetsScreen(budgetsViewModel) { navController.popBackStack() }
                }
                composable(Screen.Dream.route) {
                    DreamScreen(dreamViewModel) { navController.popBackStack() }
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