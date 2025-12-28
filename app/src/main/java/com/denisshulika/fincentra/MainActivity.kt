package com.denisshulika.fincentra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.screens.IntegrationsScreen
import com.denisshulika.fincentra.ui.screens.ProfileScreen
import com.denisshulika.fincentra.ui.screens.StatsScreen
import com.denisshulika.fincentra.ui.screens.TransactionsScreen
import com.denisshulika.fincentra.ui.theme.FinCentraTheme

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Transactions.route,
                    onClick = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    label = { Text(Screen.Transactions.title) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Stats.route,
                    onClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    label = { Text(Screen.Transactions.title) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Integrations.route,
                    onClick = {
                        navController.navigate(Screen.Integrations.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    label = { Text(Screen.Transactions.title) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    label = { Text(Screen.Transactions.title) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Transactions.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Transactions.route) { TransactionsScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Integrations.route) { IntegrationsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}