package com.denisshulika.fincentra.ui.components.integrations

import android.content.Context
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.BankAccount
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
    val wiseToken by viewModel.wiseToken.collectAsStateWithLifecycle()

    val isMonobank = bank.id == BankProviders.MONOBANK
    val isWise = bank.id == BankProviders.WISE

    val bankAccounts = accounts.filter { it.provider == bank.id }
    val isAlreadyConnected = bankAccounts.isNotEmpty()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = bank.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(bank.nameRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                modifier = Modifier.zIndex(1f),
                onClick = { viewModel.closeBankDetails() }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.bank_details_content_close_desc)
                )
            }
        }

        if (!isAlreadyConnected) {
            when (bank.id) {
                BankProviders.GOOGLE_WALLET -> WalletSetupContent(viewModel, context)
                BankProviders.MONOBANK -> MonobankSetupContent(viewModel, monoToken)
                BankProviders.WISE -> WiseSetupContent(viewModel, wiseToken)
                else -> EuropeanSetupContent(viewModel, bank)
            }
        } else {
            when {
                isMonobank -> MonobankConnectedContent(viewModel, bankAccounts)
                isWise -> WiseConnectedContent(viewModel, bankAccounts)
                else -> EuropeanConnectedContent(viewModel, bank, bankAccounts)
            }
        }
    }
}


@Composable
fun SyncStatusBlock(status: String, progress: Float) {
    val displayStatus = when {
        status == "UPDATING_BALANCES" -> stringResource(R.string.bank_details_content_status_updating)
        status == "SYNCING_ASSETS" -> stringResource(R.string.bank_details_content_status_assets)
        status == "DONE" -> stringResource(R.string.bank_details_content_status_done)
        status.startsWith("SYNCING_ACC:") -> stringResource(
            R.string.bank_details_content_status_syncing,
            status.removePrefix("SYNCING_ACC:")
        )

        status.startsWith("COOLDOWN:") -> stringResource(
            R.string.bank_details_content_status_cooldown,
            status.removePrefix("COOLDOWN:").toIntOrNull() ?: 0
        )

        else -> status
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
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
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun WalletSetupContent(viewModel: IntegrationsViewModel, context: Context) {
    val isWalletUserEnabled by viewModel.isWalletUserEnabled.collectAsStateWithLifecycle()
    val isSystemPermissionGranted by viewModel.isWalletEnabled.collectAsStateWithLifecycle()

    Column {
        Text(
            stringResource(R.string.bank_details_content_wallet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.bank_details_content_wallet_desc),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Surface(
            color = if (isSystemPermissionGranted) MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.2f
            ) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
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
                Text(
                    text = if (isSystemPermissionGranted) stringResource(R.string.bank_details_content_wallet_access_granted)
                    else stringResource(R.string.bank_details_content_wallet_access_required),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.bank_details_content_wallet_enable_automation),
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = isWalletUserEnabled,
                onCheckedChange = { viewModel.toggleWalletSync(it, context) })
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
                Text(stringResource(R.string.bank_details_content_wallet_btn_grant))
            }
        }
    }
}

@Composable
fun MonobankSetupContent(viewModel: IntegrationsViewModel, token: String) {
    val isMonoLoading = viewModel.isMonoLoading.collectAsStateWithLifecycle()

    Column {
        val annotatedString = buildAnnotatedString {
            append(stringResource(R.string.bank_details_content_mono_get_access))
            withLink(LinkAnnotation.Url("https://api.monobank.ua/")) {
                append(" ")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(R.string.bank_details_content_mono_personal_cabinet))
                }
            }
        }
        Text(text = annotatedString)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = token,
            onValueChange = viewModel::onMonobankTokenChange,
            label = { Text(stringResource(R.string.bank_details_content_mono_token_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = viewModel::connectMonobankAccount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp),
            enabled = token.isNotBlank() && !isMonoLoading.value,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isMonoLoading.value) CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White
            )
            else Text(
                stringResource(R.string.bank_details_content_mono_btn_connect),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WiseSetupContent(viewModel: IntegrationsViewModel, token: String) {
    val isWiseLoading = viewModel.isWiseLoading.collectAsStateWithLifecycle()

    Column {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.bank_details_content_wise_setup_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.bank_details_content_wise_setup_desc),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = token,
            onValueChange = viewModel::onWiseTokenChange,
            label = { Text(stringResource(R.string.bank_details_content_wise_token_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Button(
            onClick = viewModel::connectWiseAccount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp),
            enabled = token.isNotBlank() && !isWiseLoading.value,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isWiseLoading.value) CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White
            )
            else Text(
                stringResource(R.string.bank_details_content_wise_btn_connect),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EuropeanSetupContent(
    viewModel: IntegrationsViewModel,
    bank: BankProviderInfo
) {
    val isEuroLoading = viewModel.isEuroLoading.collectAsStateWithLifecycle()
    val euroSyncStatus = viewModel.euroSyncStatus.collectAsStateWithLifecycle()

    Column {
        Text(stringResource(R.string.bank_details_content_euro_setup_desc, bank.nameRes))
        Button(
            onClick = {
                val provider = EuropeanDemoBanks.find { it.id == bank.id }
                provider?.let { viewModel.connectEuropeanBank(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp),
            enabled = !isEuroLoading.value,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isEuroLoading.value && euroSyncStatus.value.isEmpty()) CircularProgressIndicator(
                modifier = Modifier.size(
                    24.dp
                ), color = Color.White
            )
            else Text(
                stringResource(R.string.bank_details_content_euro_btn_connect),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonobankConnectedContent(
    viewModel: IntegrationsViewModel,
    bankAccounts: List<BankAccount>
) {
    val isMonoLoading = viewModel.isMonoLoading.collectAsStateWithLifecycle()
    val monoSyncStatus = viewModel.monoSyncStatus.collectAsState()
    val monoSyncProgress = viewModel.monoSyncProgress.collectAsState()


    if (monoSyncStatus.value.isNotBlank()) {
        SyncStatusBlock(monoSyncStatus.value, monoSyncProgress.value)
    }

    Spacer(Modifier.height(24.dp))

    Column {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.bank_details_content_mono_sync_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = stringResource(R.string.bank_details_content_mono_select_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(bankAccounts) { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            if (!isMonoLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = account.selected,
                        onCheckedChange = {
                            if (!isMonoLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                        enabled = !isMonoLoading.value
                    )
                    Column {
                        Text(account.name, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(
                                R.string.bank_details_content_account_format,
                                account.balance,
                                CurrencyMapper.getSymbol(account.currencyCode)
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = viewModel::refreshMonobankAccounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isMonoLoading.value,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.bank_details_content_mono_btn_refresh))
            }

            Button(
                onClick = viewModel::confirmMonobankSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isMonoLoading.value,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.bank_details_content_mono_btn_save),
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { viewModel.disconnectMonobank() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isMonoLoading.value
            ) {
                Text(
                    stringResource(R.string.bank_details_content_mono_btn_disconnect),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun WiseConnectedContent(
    viewModel: IntegrationsViewModel,
    bankAccounts: List<BankAccount>
) {
    val isWiseLoading = viewModel.isWiseLoading.collectAsStateWithLifecycle()

    Column {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.bank_details_content_wise_sync_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = stringResource(R.string.bank_details_content_wise_select_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(bankAccounts) { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            if (!isWiseLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = account.selected,
                        onCheckedChange = {
                            if (!isWiseLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                        enabled = !isWiseLoading.value
                    )
                    Column {
                        Text(account.name, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(
                                R.string.bank_details_content_account_format,
                                account.balance,
                                CurrencyMapper.getSymbol(account.currencyCode)
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = viewModel::confirmWiseSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isWiseLoading.value,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.bank_details_content_wise_btn_save),
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { viewModel.disconnectWise() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isWiseLoading.value
            ) {
                Text(
                    stringResource(R.string.bank_details_content_wise_btn_disconnect),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EuropeanConnectedContent(
    viewModel: IntegrationsViewModel,
    bank: BankProviderInfo,
    bankAccounts: List<BankAccount>
) {
    val isEuroLoading = viewModel.isEuroLoading.collectAsStateWithLifecycle()

    Column {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.bank_details_content_euro_sync_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = stringResource(R.string.bank_details_content_euro_select_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(bankAccounts) { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            if (!isEuroLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = account.selected,
                        onCheckedChange = {
                            if (!isEuroLoading.value) viewModel.toggleAccountSelection(
                                account.id
                            )
                        },
                        enabled = !isEuroLoading.value
                    )
                    Column {
                        Text(account.name, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(
                                R.string.bank_details_content_account_format,
                                account.balance,
                                CurrencyMapper.getSymbol(account.currencyCode)
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.confirmEuropeanSelection(bank.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isEuroLoading.value,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.bank_details_content_euro_btn_save),
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { viewModel.disconnectEuropeanBank(bank.id) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isEuroLoading.value
            ) {
                Text(
                    stringResource(R.string.bank_details_content_euro_btn_disconnect),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}