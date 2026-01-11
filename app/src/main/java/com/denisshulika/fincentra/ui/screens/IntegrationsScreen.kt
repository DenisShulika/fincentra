package com.denisshulika.fincentra.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.events.IntegrationsUiEvent
import com.denisshulika.fincentra.data.models.ui.SupportedBanks
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.integrations.BankDetailsContent
import com.denisshulika.fincentra.ui.components.integrations.BankGridItem
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel

@Composable
fun IntegrationsScreen(
    viewModel: IntegrationsViewModel,
    onBack: () -> Unit
) {
    val selectedBank by viewModel.selectedBank.collectAsStateWithLifecycle()
    val isConnected by viewModel.isBankConnected.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshConnectionStatus()
        viewModel.events.collect { event ->
            when (event) {
                is IntegrationsUiEvent.OpenUrl -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
                }

                is IntegrationsUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(
                        context,
                        event.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FinCentraTopBar(
                    title = "Банки та рахунки",
                    isTopLevelScreen = false,
                    onNavigationClick = onBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Підключення",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(SupportedBanks) { bank ->
                        BankGridItem(
                            bank = bank,
                            isConnected = isConnected,
                            isLoading = isLoading,
                            onClick = { viewModel.selectBank(bank) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedBank != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = !isLoading) { viewModel.closeBankDetails() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(24.dp)
                ) {
                    selectedBank?.let { bank ->
                        BankDetailsContent(bank, viewModel)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Відключити банк?", fontWeight = FontWeight.Bold) },
            text = { Text("Нові транзакції не будуть завантажуватись. Налаштування рахунків видаляться.") },
            confirmButton = {
                TextButton(onClick = { viewModel.removeMonobankIntegration() }) {
                    Text(
                        "Так, відключити",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text("Скасувати")
                }
            }
        )
    }
}