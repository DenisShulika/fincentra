package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.components.*
import com.denisshulika.fincentra.viewmodels.*
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    statsViewModel: StatsViewModel,
    transactionsViewModel: TransactionsViewModel,
    budgetsViewModel: BudgetsViewModel,
    dreamViewModel: DreamViewModel,
    aiViewModel: AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController,
    onNavigateToTransactions: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val uiState by statsViewModel.uiState.collectAsStateWithLifecycle()
    val budgets by budgetsViewModel.budgetProgressList.collectAsStateWithLifecycle()
    val transactions by transactionsViewModel.transactions.collectAsStateWithLifecycle()
    val dreamState by dreamViewModel.dreamProgress.collectAsStateWithLifecycle()

    val aiAdvice by aiViewModel.adviceText.collectAsStateWithLifecycle()
    val isAiLoading by aiViewModel.isLoading.collectAsStateWithLifecycle()

    val user = DependencyProvider.authRepository.auth.currentUser
    val currentUserName = user?.displayName?.substringBefore(" ") ?: "User"
    val pagerState = rememberPagerState(pageCount = { uiState.currencyData.size })

    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(pagerState.currentPage) {
        statsViewModel.selectCurrency(pagerState.currentPage)
    }

    val viewingTx by transactionsViewModel.viewingTransaction.collectAsStateWithLifecycle()
    viewingTx?.let { tx ->
        TransactionDetailSheet(transaction = tx) {
            transactionsViewModel.closeTransactionDetails()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinCentraTopBar(
                title = "FinCentra",
                isTopLevelScreen = true,
                onNavigationClick = onOpenDrawer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                    Text(
                        text = stringResource(R.string.home_greeting, currentUserName),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_status_title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.currencyData.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) { pageIndex ->
                            val currencyStats = uiState.currencyData.getOrNull(pageIndex)
                            if (currencyStats != null) {
                                val symbol = CurrencyMapper.getSymbol(currencyStats.currencyCode)
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                                        alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                                        scaleY = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                                    }
                                ) {
                                    BalanceFlowCard(stats = currencyStats, symbol = symbol)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = !isAiLoading) { aiViewModel.fetchAdvice(currentUserName) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF16A34A), Color(0xFF065F46))))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = Color.White.copy(alpha = if (isAiLoading) 0.1f else 0.2f)) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = if (isAiLoading) iconAlpha else 1f),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.home_ai_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                val displayMessage = when {
                                    isAiLoading && aiAdvice.isEmpty() -> stringResource(R.string.ai_loading)
                                    aiAdvice == "ERROR_STATE" -> stringResource(R.string.ai_error_internet)
                                    aiAdvice.isEmpty() -> stringResource(R.string.ai_click_prompt)
                                    else -> aiAdvice
                                }

                                Text(
                                    text = displayMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            dreamState?.let { progressData ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(text = stringResource(R.string.home_dream_goal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Dream.route) },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(progress = { progressData.progress }, modifier = Modifier.size(64.dp), strokeCap = StrokeCap.Round, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    Text(text = progressData.dream.iconEmoji, fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = progressData.dream.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "Зібрано ${(progressData.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            val critical = budgets.filter { it.progress > 0.6f }.sortedByDescending { it.progress }
            if (critical.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text(text = stringResource(R.string.home_critical_limits), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = stringResource(R.string.home_all_limits), modifier = Modifier.clickable { onNavigateToBudgets() }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        critical.take(2).forEach { item ->
                            BudgetProgressItem(item = item, onClick = onNavigateToBudgets)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(text = stringResource(R.string.home_recent_tx), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.home_view_all), modifier = Modifier.clickable { onNavigateToTransactions() }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val lastThree = transactions.take(3)
                    if (lastThree.isEmpty()) {
                        Text(text = stringResource(R.string.home_no_transactions), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = TextAlign.Center)
                    } else {
                        lastThree.forEach { tx -> TransactionItem(transaction = tx, onClick = { transactionsViewModel.showTransactionDetails(tx) }, onLongClick = {}) }
                    }
                }
            }
        }
    }
}