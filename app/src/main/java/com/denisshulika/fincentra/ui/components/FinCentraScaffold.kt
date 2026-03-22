package com.denisshulika.fincentra.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.launch

@Composable
fun FinCentraScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
    topBar: @Composable (onOpenDrawer: () -> Unit) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user = DependencyProvider.authRepository.getCurrentUser()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            FinCentraDrawer(
                user = user,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch {
                        drawerState.snapTo(DrawerValue.Closed)
                        onNavigate(route)
                    }
                },
                onClose = { scope.launch { drawerState.close() } },
                onLogout = {
                    scope.launch {
                        drawerState.snapTo(DrawerValue.Closed)
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = { topBar { scope.launch { drawerState.open() } } },
            bottomBar = bottomBar,
            containerColor = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}