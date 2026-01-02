package com.denisshulika.fincentra.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.viewmodels.IntegrationsUiEvent
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel

@Composable
fun IntegrationsScreen(viewModel: IntegrationsViewModel) {
    val isConnected by viewModel.isBankConnected.collectAsStateWithLifecycle()
    val isInputVisible by viewModel.isMonoInputVisible.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val monoToken by viewModel.monoToken.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    val lastSync by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    lastSync?.let { timestamp ->
        val date = java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))

        Text(
            text = "Останнє фонове оновлення: $date",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is IntegrationsUiEvent.OpenUrl -> {
                    if (event.url.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        context.startActivity(intent)
                    }
                }
                is IntegrationsUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val showBottomSheet by viewModel.showAccountSelection.collectAsStateWithLifecycle()

    if (showBottomSheet) {
        AccountSelectionSheet(viewModel)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Підключення банків", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text("Monobank", style = MaterialTheme.typography.titleLarge)

                    if (isConnected) {
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isConnected) {
                    Button(
                        onClick = { viewModel.syncMonobank() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Синхронізувати зараз")
                        }
                    }

                    if (syncStatus.isNotBlank()) {
                        Text(
                            text = syncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.openAccountSettings() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Налаштувати рахунки")
                    }

                    TextButton(
                        onClick = { viewModel.disconnectBank() },
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Відключити банк", color = if (isLoading) Color.Gray else Color.Red)
                    }
                } else {
                    if (!isInputVisible) {
                        Button(
                            onClick = { viewModel.toggleMonoInput(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Підключити")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openMonobankAuth() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отримати токен")
                        }
                        OutlinedTextField(
                            value = monoToken,
                            onValueChange = { viewModel.onTokenChange(it) },
                            label = { Text("Введіть токен") },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = { viewModel.connectNewBank() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Зберегти та підключити")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionSheet(viewModel: IntegrationsViewModel) {
    val accounts by viewModel.availableAccounts.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.toggleAccountBottomSheet(false) },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Виберіть рахунки для відстеження", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(accounts) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAccountSelection(account.id) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = account.selected,
                            onCheckedChange = { viewModel.toggleAccountSelection(account.id) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(account.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${account.balance} ${CurrencyMapper.getSymbol(account.currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.confirmAccountSelection() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Підтвердити вибір")
            }
        }
    }
}