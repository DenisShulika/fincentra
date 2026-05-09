package com.denisshulika.fincentra.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.events.IntegrationsUiEvent
import com.denisshulika.fincentra.data.models.ui.BankProviderInfo
import com.denisshulika.fincentra.data.models.ui.SupportedBanks
import com.denisshulika.fincentra.data.util.BankProviders
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
    val showDeleteDialog by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()

    val isEuropeanMode by viewModel.isEuropeanMode.collectAsStateWithLifecycle()
    val europeanBanks by viewModel.europeanBanks.collectAsStateWithLifecycle()

    val isWalletSystemEnabled by viewModel.isWalletEnabled.collectAsStateWithLifecycle()
    val isWalletUserEnabled by viewModel.isWalletUserEnabled.collectAsStateWithLifecycle()

    val isEuroLoading by viewModel.isEuroLoading.collectAsStateWithLifecycle()
    val loadingBankId by viewModel.loadingBankId.collectAsStateWithLifecycle()
    val savedAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle()

    val isMonoLoading by viewModel.isMonoLoading.collectAsStateWithLifecycle()
    val isWiseLoading by viewModel.isWiseLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshConnectionStatus()
        viewModel.checkWalletStatus(context)
        viewModel.events.collect { event ->
            when (event) {
                is IntegrationsUiEvent.OpenUrl -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
                }

                is IntegrationsUiEvent.ShowToast -> {
                    val message = context.resources.getString(event.messageRes)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FinCentraTopBar(
                    title = stringResource(R.string.integrations_screen_title),
                    isTopLevelScreen = false,
                    onNavigationClick = {
                        viewModel.closeBankDetails()
                        onBack()
                    }
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
                    text = stringResource(R.string.integrations_screen_connection_header),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = if (isEuropeanMode) 1 else 0,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = !isEuropeanMode,
                        onClick = { viewModel.toggleEuropeanMode(false) }
                    ) {
                        Text(
                            text = stringResource(R.string.integrations_screen_tab_ukraine),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Tab(
                        selected = isEuropeanMode,
                        onClick = { viewModel.toggleEuropeanMode(true) }
                    ) {
                        Text(
                            text = stringResource(R.string.integrations_screen_tab_europe),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isEuropeanMode) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(SupportedBanks) { bank ->
                            val currentLoading = when (bank.id) {
                                BankProviders.MONOBANK -> isMonoLoading
                                BankProviders.WISE -> isWiseLoading
                                else -> false
                            }

                            val isConnected = if (bank.id == BankProviders.GOOGLE_WALLET) {
                                isWalletUserEnabled
                            } else {
                                savedAccounts.any { it.provider == bank.id }
                            }

                            BankGridItem(
                                bank = bank,
                                isConnected = isConnected,
                                isLoading = currentLoading,
                                onClick = { viewModel.selectBank(bank, context) }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(europeanBanks) { provider ->
                            val isConnected = savedAccounts.any { it.provider == provider.id }
                            val isThisBankLoading = isEuroLoading && loadingBankId == provider.id

                            BankGridItem(
                                bank = BankProviderInfo(
                                    id = provider.id,
                                    nameRes = provider.nameRes,
                                    logo = R.drawable.ic_launcher_foreground,
                                    brandColor = provider.brandColor,
                                    subtitleRes = provider.countryNameRes
                                ),
                                isConnected = isConnected,
                                isLoading = isThisBankLoading,
                                onClick = {
                                    viewModel.selectBank(
                                        BankProviderInfo(
                                            id = provider.id,
                                            nameRes = provider.nameRes,
                                            logo = R.drawable.ic_launcher_foreground,
                                            brandColor = provider.brandColor,
                                            subtitleRes = provider.countryNameRes
                                        ),
                                        context
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (selectedBank != null) {
            Dialog(
                onDismissRequest = { viewModel.closeBankDetails() },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            title = {
                Text(
                    text = stringResource(R.string.integrations_screen_delete_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.integrations_screen_delete_desc)) },
            confirmButton = {
                TextButton(onClick = { viewModel.disconnectMonobank() }) {
                    Text(
                        text = stringResource(R.string.integrations_screen_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text(stringResource(R.string.integrations_screen_btn_cancel))
                }
            }
        )
    }
}