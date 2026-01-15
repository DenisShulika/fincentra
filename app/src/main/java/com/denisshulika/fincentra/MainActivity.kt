package com.denisshulika.fincentra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.data.util.PrefConstants
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.screens.*
import com.denisshulika.fincentra.ui.theme.FinCentraTheme
import com.denisshulika.fincentra.viewmodels.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = DependencyProvider.financeRepository

        splashScreen.setKeepOnScreenCondition {
            val user = DependencyProvider.authRepository.getCurrentUser()
            if (user != null) {
                repository.transactions.value.isEmpty()
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

    val isAuthScreen = currentRoute in listOf(Screen.Login.route, Screen.Register.route, Screen.Onboarding.route)
    val isTopLevelScreen = currentRoute in listOf(Screen.Home.route, Screen.Transactions.route, Screen.Stats.route)

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
            if (!isAuthScreen) {
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

                    val drawerItems = listOf(Screen.Profile, Screen.Integrations, Screen.Budgets, Screen.Dream)
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
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
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
                if (!isAuthScreen) {
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
                                        text = screen.title,
                                        fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(screen.icon, null) },
                                onClick = { navigateWithClearStack(screen.route) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
                    bottom = if (!isAuthScreen) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen {
                        prefs.edit().putBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, true).apply()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToMain = {
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
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
                            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
            }
        }
    }
}