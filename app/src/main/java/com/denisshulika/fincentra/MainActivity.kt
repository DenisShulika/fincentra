package com.denisshulika.fincentra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.screens.IntegrationsScreen
import com.denisshulika.fincentra.ui.screens.ProfileScreen
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false, // Поки що просто статично
                    onClick = { navController.navigate(Screen.Transactions.route) },
                    label = { Text("Транзакції") },
                    icon = { /* Тут потім будуть іконки */ }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Integrations.route) },
                    label = { Text("Банки") },
                    icon = { }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    label = { Text("Профіль") },
                    icon = { }
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
            composable(Screen.Integrations.route) { IntegrationsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}