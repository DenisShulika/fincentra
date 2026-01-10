package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.TransactionDetailSheet
import com.denisshulika.fincentra.ui.components.TransactionItem
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.DreamViewModel
import com.denisshulika.fincentra.viewmodels.StatsViewModel
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun HomeScreen(
    statsViewModel: StatsViewModel,
    transactionsViewModel: TransactionsViewModel,
    budgetsViewModel: BudgetsViewModel,
    dreamViewModel: DreamViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val uiState by statsViewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by statsViewModel.selectedCurrencyIndex.collectAsStateWithLifecycle()
    val budgets by budgetsViewModel.budgetProgressList.collectAsStateWithLifecycle()
    val transactions by transactionsViewModel.transactions.collectAsStateWithLifecycle()
    val user by DependencyProvider.authRepository.auth.currentUser.let { mutableStateOf(it) }
    val dreamState by dreamViewModel.dreamProgress.collectAsStateWithLifecycle()

    val currentStats = uiState.currencyData.getOrNull(selectedIndex)

    val viewingTx by transactionsViewModel.viewingTransaction.collectAsStateWithLifecycle()
    viewingTx?.let { tx ->
        TransactionDetailSheet(transaction = tx) {
            transactionsViewModel.closeTransactionDetails()
        }
    }

    Scaffold(
        topBar = {
            FinCentraTopBar(
                title = "FinCentra",
                isTopLevelScreen = true,
                onNavigationClick = onOpenDrawer
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    Text(
                        "Привіт, ${user?.displayName ?: "користувачу"}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    if (uiState.currencyData.size > 1) {
                        TabRow(
                            selectedTabIndex = selectedIndex,
                            containerColor = Color.Transparent
                        ) {
                            uiState.currencyData.forEachIndexed { index, data ->
                                Tab(
                                    selected = selectedIndex == index,
                                    onClick = { statsViewModel.selectCurrency(index) },
                                    text = { Text(CurrencyMapper.getCodeName(data.currencyCode)) }
                                )
                            }
                        }
                    }
                }

                if (currentStats != null) {
                    item {
                        BalanceFlowCard(
                            currentStats,
                            CurrencyMapper.getSymbol(currentStats.currencyCode)
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.2f
                            )
                        )
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Порада від ШІ",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Аналізую твої витрати... Поради з'являться скоро.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                dreamState?.let { progressData ->
                    item {
                        Text(
                            "Твоя мрія",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.Dream.route) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                    alpha = 0.2f
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { progressData.progress },
                                        modifier = Modifier.size(54.dp),
                                        strokeWidth = 4.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Text(
                                        progressData.dream.iconEmoji,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        progressData.dream.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val percent = (progressData.progress * 100).toInt()
                                    Text(
                                        "Зібрано $percent%",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }

                val critical = budgets.filter { it.progress > 0.6f }
                if (critical.isNotEmpty()) {
                    item {
                        Text(
                            "Увага до лімітів",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        critical.take(2).forEach { item ->
                            BudgetProgressItem(item = item, onClick = onNavigateToBudgets)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    Text(
                        "Останні операції",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(transactions.take(3)) { tx ->
                    TransactionItem(
                        tx,
                        onClick = { transactionsViewModel.showTransactionDetails(tx) },
                        onLongClick = {})
                }
            }
        }
    }
}