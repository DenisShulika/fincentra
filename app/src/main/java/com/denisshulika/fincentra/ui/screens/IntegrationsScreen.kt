package com.denisshulika.fincentra.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.BankProviderInfo
import com.denisshulika.fincentra.data.models.SupportedBanks
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.viewmodels.IntegrationsUiEvent
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel

@Composable
fun IntegrationsScreen(viewModel: IntegrationsViewModel) {
    val selectedBank by viewModel.selectedBank.collectAsStateWithLifecycle()
    val isConnected by viewModel.isBankConnected.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is IntegrationsUiEvent.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }
                is IntegrationsUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (selectedBank != null) {
        Dialog(
            onDismissRequest = { viewModel.closeBankDetails() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.75f),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                selectedBank?.let { bank ->
                    BankDetailsContent(bank, viewModel)
                }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Підключення банків", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

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

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Підключення банків", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(2), /* ... налаштування ... */) {
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

        AnimatedVisibility(
            visible = selectedBank != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxSize().clickable { viewModel.closeBankDetails() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.8f)
                            .clickable(enabled = false) { },
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        selectedBank?.let { bank ->
                            BankDetailsContent(bank, viewModel)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text("Відключити банк?") },
            text = { Text("Транзакції залишаться, але нові не будуть завантажуватись.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.disconnectBank()
                    viewModel.dismissDeleteConfirmation()
                    viewModel.closeBankDetails()
                }) { Text("Відключити", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) { Text("Скасувати") }
            }
        )
    }
}

@Composable
private fun BankDetailsContent(bank: BankProviderInfo, viewModel: IntegrationsViewModel) {
    val isConnected by viewModel.isBankConnected.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val monoToken by viewModel.monoToken.collectAsStateWithLifecycle()
    val accounts by viewModel.availableAccounts.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = bank.logo),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(bank.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.closeBankDetails() }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        if (isLoading && syncProgress > 0) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    text = "Загальний прогрес: ${(syncProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = bank.brandColor,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { syncProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
                    color = bank.brandColor,
                    trackColor = bank.brandColor.copy(alpha = 0.2f)
                )
            }
        }

        if (syncStatus.isNotBlank()) {
            Text(
                text = syncStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Surface(
            color = Color(0xFFFFF9C4),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Обмеження: 1 запит виписки на 60 секунд. Будь ласка, зачекайте після натискання.", style = MaterialTheme.typography.bodySmall)
            }
        }

        if (isConnected) {
            Text("Ваші рахунки:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(accounts) { account ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAccountSelection(account.id) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = account.selected, onCheckedChange = { viewModel.toggleAccountSelection(account.id) })
                        Text(account.name, modifier = Modifier.weight(1f))
                        Text("${account.balance} ${CurrencyMapper.getSymbol(account.currencyCode)}")
                    }
                }
            }

            OutlinedButton(
                onClick = { viewModel.refreshAccountsInDetails() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) { Text("Оновити список рахунків") }

            Button(
                onClick = { viewModel.confirmAccountSelection() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = !isLoading
            ) { Text("Синхронізувати транзакції") }

            TextButton(
                onClick = { viewModel.askDeleteConfirmation() },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) { Text("Відключити підключення", color = Color.Red) }

        } else {
            Spacer(Modifier.height(16.dp))

            val annotatedString = buildAnnotatedString {
                append("Щоб підключити банк, отримайте токен у ")
                withLink(LinkAnnotation.Url("https://api.monobank.ua/")) {
                    withStyle(style = SpanStyle(color = bank.brandColor, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                        append("Особистому кабінеті")
                    }
                }
            }
            Text(text = annotatedString)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = monoToken,
                onValueChange = { viewModel.onTokenChange(it) },
                label = { Text("API Токен") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { viewModel.connectNewBank() },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                enabled = monoToken.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Підключити")
            }
        }
    }
}

@Composable
fun BankGridItem(bank: BankProviderInfo, isConnected: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(140.dp).clickable { onClick() }
            .border(2.dp, if (isConnected) bank.brandColor else Color.Transparent, MaterialTheme.shapes.medium)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = bank.logo),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text(bank.name, style = MaterialTheme.typography.titleMedium)
            }

            if (isConnected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = bank.brandColor,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 4.dp,
                    color = bank.brandColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}