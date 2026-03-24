package com.denisshulika.fincentra

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.data.util.LanguageManager
import com.denisshulika.fincentra.data.util.PrefConstants
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.components.FinCentraScaffold
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.transactions.TransactionsTopBar
import com.denisshulika.fincentra.ui.screens.AboutScreen
import com.denisshulika.fincentra.ui.screens.BudgetsScreen
import com.denisshulika.fincentra.ui.screens.DreamScreen
import com.denisshulika.fincentra.ui.screens.HomeScreen
import com.denisshulika.fincentra.ui.screens.IntegrationsScreen
import com.denisshulika.fincentra.ui.screens.LoadingScreen
import com.denisshulika.fincentra.ui.screens.LoginScreen
import com.denisshulika.fincentra.ui.screens.OnboardingScreen
import com.denisshulika.fincentra.ui.screens.ProfileScreen
import com.denisshulika.fincentra.ui.screens.RegisterScreen
import com.denisshulika.fincentra.ui.screens.SettingsScreen
import com.denisshulika.fincentra.ui.screens.StatsScreen
import com.denisshulika.fincentra.ui.screens.TransactionsScreen
import com.denisshulika.fincentra.ui.theme.FinCentraTheme
import com.denisshulika.fincentra.viewmodels.AuthViewModel
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.DreamViewModel
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel
import com.denisshulika.fincentra.viewmodels.ProfileViewModel
import com.denisshulika.fincentra.viewmodels.SettingsViewModel
import com.denisshulika.fincentra.viewmodels.StatsViewModel
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        LanguageManager.initLocale()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val appTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()

            FinCentraTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isDataReady by DependencyProvider.financeRepository.isInitialLoadComplete.collectAsStateWithLifecycle()
                    val user = DependencyProvider.authRepository.getCurrentUser()

                    when {
                        user == null -> MainScreen(settingsViewModel)
                        !isDataReady -> LoadingScreen()
                        else -> MainScreen(settingsViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val prefs =
        remember { context.getSharedPreferences(PrefConstants.PREFS_NAME, Context.MODE_PRIVATE) }
    val isOnboardingCompleted =
        remember { prefs.getBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, false) }

    val navController = rememberNavController()
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
        if (!isOnboardingCompleted) Screen.Onboarding.route
        else if (currentUser != null) Screen.Home.route
        else Screen.Login.route
    }

    val navigateWithClearStack: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val globalBottomBar: @Composable () -> Unit = {
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
                            stringResource(screen.titleRes),
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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen {
                prefs.edit().putBoolean(PrefConstants.KEY_IS_ONBOARDING_COMPLETED, true).apply()
                navController.navigate(Screen.Login.route) { popUpTo(0) }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel,
                onNavigateToMain = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) })
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel,
                onNavigateToMain = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                onBackToLogin = { navController.popBackStack() })
        }

        composable(Screen.Home.route) {
            FinCentraScaffold(currentRoute,
                { navController.navigate(it) },
                { settingsViewModel.logout { navController.navigate(Screen.Login.route) { popUpTo(0) } } },
                globalBottomBar,
                topBar = { onOpen -> FinCentraTopBar("FinCentra", true, onOpen) }
            ) { innerPadding ->
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    statsViewModel,
                    transactionsViewModel,
                    budgetsViewModel,
                    dreamViewModel,
                    navController = navController,
                    onNavigateToTransactions = { navigateWithClearStack(Screen.Transactions.route) },
                    onOpenDrawer = {},
                    onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) }
                )
            }
        }

        composable(Screen.Transactions.route) {
            FinCentraScaffold(currentRoute,
                { navController.navigate(it) },
                { settingsViewModel.logout { navController.navigate(Screen.Login.route) { popUpTo(0) } } },
                globalBottomBar,
                topBar = { onOpen -> TransactionsTopBar(transactionsViewModel, {}, onOpen, {}, {}) }
            ) { innerPadding ->
                TransactionsScreen(
                    transactionsViewModel,
                    onOpenDrawer = {},
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(Screen.Stats.route) {
            FinCentraScaffold(currentRoute,
                { navController.navigate(it) },
                { settingsViewModel.logout { navController.navigate(Screen.Login.route) { popUpTo(0) } } },
                globalBottomBar,
                topBar = { onOpen ->
                    FinCentraTopBar(
                        stringResource(R.string.nav_stats),
                        true,
                        onOpen
                    )
                }
            ) { innerPadding ->
                StatsScreen(
                    statsViewModel,
                    onOpenDrawer = {},
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) })
        }
        composable(Screen.Integrations.route) {
            IntegrationsScreen(integrationsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Budgets.route) {
            BudgetsScreen(budgetsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Dream.route) {
            DreamScreen(dreamViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsViewModel,
                onBack = { navController.popBackStack() },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } })
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}