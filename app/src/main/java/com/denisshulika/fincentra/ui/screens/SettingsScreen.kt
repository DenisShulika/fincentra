package com.denisshulika.fincentra.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showPassDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    if (showPassDialog) {
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text("Новий пароль", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Введіть пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changePassword(newPassword) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                        showPassDialog = false
                        newPassword = ""
                    }
                ) { Text("Оновити") }
            },
            dismissButton = {
                TextButton(onClick = { showPassDialog = false }) { Text("Скасувати") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Видалити акаунт?", fontWeight = FontWeight.Bold) },
            text = { Text("Всі ваші дані в хмарі будуть безповоротно втрачені.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(
                            onDeleted = onLogout,
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    }
                ) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Скасувати") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinCentraTopBar(title = "Налаштування", isTopLevelScreen = false, onNavigationClick = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Вигляд та мова") {
                SettingsRow(
                    title = "Темна тема",
                    icon = Icons.Default.DarkMode,
                    action = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleTheme(it) },
                            enabled = !isLoading
                        )
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                SettingsRow(
                    title = "Мова додатку",
                    icon = Icons.Default.Language,
                    action = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("uk", "en", "pl", "de").forEach { lang ->
                                Text(
                                    text = lang.uppercase(),
                                    modifier = Modifier.clickable { viewModel.setLanguage(lang) }.padding(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                )
            }

            SettingsSection(title = "Безпека акаунта") {
                SettingsRow(
                    title = "Змінити пароль",
                    icon = Icons.Default.Lock,
                    onClick = { if (!isLoading) showPassDialog = true }
                )

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                SettingsRow(
                    title = "Видалити мій акаунт",
                    icon = Icons.Default.DeleteForever,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { if (!isLoading) showDeleteDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                Text("Вийти із системи", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: ImageVector,
    titleColor: Color = Color.Unspecified,
    action: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                color = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        action?.invoke()
    }
}