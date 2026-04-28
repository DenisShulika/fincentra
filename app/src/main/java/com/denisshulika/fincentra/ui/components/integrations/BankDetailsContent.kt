package com.denisshulika.fincentra.ui.components.integrations

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.ui.BankProviderInfo
import com.denisshulika.fincentra.data.models.ui.EuropeanDemoBanks
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel

@Composable
fun BankDetailsContent(
    bank: BankProviderInfo,
    viewModel: IntegrationsViewModel
) {
    val context = LocalContext.current

    val accounts by viewModel.availableAccounts.collectAsStateWithLifecycle()
    val monoToken by viewModel.monobankToken.collectAsStateWithLifecycle()

    val isMonobank = bank.id == BankProviders.MONOBANK

    val isLoading by (if (isMonobank) viewModel.isMonoLoading else viewModel.isEuroLoading).collectAsStateWithLifecycle()
    val syncStatus by (if (isMonobank) viewModel.monoSyncStatus else viewModel.euroSyncStatus).collectAsStateWithLifecycle()
    val syncProgress by (if (isMonobank) viewModel.monoSyncProgress else viewModel.euroSyncProgress).collectAsStateWithLifecycle()

    val bankAccounts = accounts.filter { it.provider == bank.id }
    val isAlreadyConnected = bankAccounts.isNotEmpty()

    val isWallet = bank.id == BankProviders.GOOGLE_WALLET
    val isWalletEnabled by viewModel.isWalletEnabled.collectAsStateWithLifecycle()
    val isWalletUserEnabled by viewModel.isWalletUserEnabled.collectAsStateWithLifecycle()
    val isSystemPermissionGranted by viewModel.isWalletEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = bank.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = bank.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.closeBankDetails() }) {
                Icon(Icons.Default.Close, null)
            }
        }

        if (syncStatus.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            val displayStatus = when {
                syncStatus == "UPDATING_BALANCES" -> stringResource(R.string.status_updating_balances)
                syncStatus == "SYNCING_ASSETS" -> "Syncing European assets..."
                syncStatus == "DONE" -> stringResource(R.string.status_done)
                syncStatus.startsWith("SYNCING_ACC:") -> stringResource(
                    R.string.status_syncing_account,
                    syncStatus.removePrefix("SYNCING_ACC:")
                )

                syncStatus.startsWith("COOLDOWN:") -> stringResource(
                    R.string.status_api_cooldown,
                    syncStatus.removePrefix("COOLDOWN:").toIntOrNull() ?: 0
                )

                else -> syncStatus
            }

            Text(
                text = displayStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { syncProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (!isAlreadyConnected) {
            if (isWallet) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Google Wallet Synchronization",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "FinCentra can automatically catch payments from Google Wallet notifications. This allows you to track banks that don't have an official API.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        color = if (isSystemPermissionGranted) MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.2f
                        )
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isSystemPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                null,
                                tint = if (isSystemPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isSystemPermissionGranted) "System Access: Granted" else "System Access: Required",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isSystemPermissionGranted) {
                                    Text(
                                        "FinCentra needs permission to read notifications.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Enable Automation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isWalletUserEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleWalletSync(enabled, context)
                            }
                        )
                    }

                    if (!isSystemPermissionGranted) {
                        Button(
                            onClick = { viewModel.openNotificationSettings(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Grant System Access")
                        }
                    }
                }
            } else if (isMonobank) {
                Spacer(Modifier.height(16.dp))
                val annotatedString = buildAnnotatedString {
                    append(stringResource(R.string.bank_get_access_text))
                    withLink(LinkAnnotation.Url("https://api.monobank.ua/")) {
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(stringResource(R.string.bank_personal_cabinet))
                        }
                    }
                }
                Text(text = annotatedString, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = monoToken,
                    onValueChange = { viewModel.onMonobankTokenChange(it) },
                    label = { Text(stringResource(R.string.bank_token_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                )

                Button(
                    onClick = { viewModel.connectMonobankAccount() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = monoToken.isNotBlank() && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    ) else Text(
                        stringResource(R.string.bank_btn_connect),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(text = "Connect your ${bank.name} account securely via Salt Edge gateway.")
                Button(
                    onClick = {
                        val provider = EuropeanDemoBanks.find { it.id == bank.id }
                        provider?.let { viewModel.connectEuropeanBank(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading && syncStatus.isEmpty()) CircularProgressIndicator(
                        modifier = Modifier.size(
                            24.dp
                        ), color = Color.White
                    )
                    else Text("Connect via Browser", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "Select Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)) {
                items(bankAccounts) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { if (!isLoading) viewModel.toggleAccountSelection(account.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = account.selected,
                            onCheckedChange = {
                                if (!isLoading) viewModel.toggleAccountSelection(
                                    account.id
                                )
                            },
                            enabled = !isLoading
                        )
                        Column {
                            Text(account.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${
                                    String.format(
                                        "%.2f",
                                        account.balance
                                    )
                                } ${CurrencyMapper.getSymbol(account.currencyCode)}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isMonobank) {
                    OutlinedButton(
                        onClick = { viewModel.refreshMonobankAccounts() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.bank_btn_refresh)) }
                }

                Button(
                    onClick = {
                        if (isMonobank) viewModel.confirmMonobankSelection()
                        else viewModel.confirmEuropeanSelection(bank.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Sync", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { viewModel.disconnectProvider(bank.id) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !isLoading
                ) {
                    Text("Disconnect Bank", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}