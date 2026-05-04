package com.denisshulika.fincentra.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.data.models.domain.User
import com.denisshulika.fincentra.navigation.Screen

@Composable
fun FinCentraDrawer(
    user: User?,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user?.displayName?.take(1) ?: user?.email?.take(1) ?: "?",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName?.ifBlank { "Financial Hero" } ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val menuItems =
                listOf(
                    Screen.Profile,
                    Screen.Integrations,
                    Screen.Budgets,
                    Screen.Dream,
                    Screen.Subscriptions
                )

            Column(modifier = Modifier.weight(1f)) {
                menuItems.forEach { screen ->
                    DrawerItem(
                        label = stringResource(screen.titleRes),
                        icon = screen.icon,
                        selected = currentRoute == screen.route,
                        onClick = { onNavigate(screen.route) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                DrawerItem(
                    label = stringResource(Screen.Settings.titleRes),
                    icon = Screen.Settings.icon,
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { onNavigate(Screen.Settings.route) }
                )

                DrawerItem(
                    label = "About App",
                    icon = Icons.Default.Info,
                    selected = currentRoute == Screen.About.route,
                    onClick = { onNavigate(Screen.About.route) }
                )

                DrawerItem(
                    label = "Sign Out",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    selected = false,
                    onClick = onLogout,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}