package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.navigation.Screen
import com.denisshulika.fincentra.ui.components.BalanceFlowCard
import com.denisshulika.fincentra.ui.components.BudgetProgressItem
import com.denisshulika.fincentra.ui.components.DynamicWeatherOverlay
import com.denisshulika.fincentra.ui.components.HealthBadge
import com.denisshulika.fincentra.ui.components.PredictionCoin
import com.denisshulika.fincentra.ui.components.TransactionDetailSheet
import com.denisshulika.fincentra.ui.components.TransactionItem
import com.denisshulika.fincentra.viewmodels.AiViewModel
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.DreamViewModel
import com.denisshulika.fincentra.viewmodels.SettingsViewModel
import com.denisshulika.fincentra.viewmodels.StatsViewModel
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel,
    transactionsViewModel: TransactionsViewModel,
    budgetsViewModel: BudgetsViewModel,
    dreamViewModel: DreamViewModel,
    settingsViewModel: SettingsViewModel,
    aiViewModel: AiViewModel = viewModel(),
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

    LaunchedEffect(Unit) {
        settingsViewModel.checkDailyPrediction()
    }

    val prediction by settingsViewModel.dailyPrediction.collectAsStateWithLifecycle()
    val isFlipped by settingsViewModel.isCoinFlipped.collectAsStateWithLifecycle()

    LaunchedEffect(pagerState.currentPage) {
        statsViewModel.selectCurrency(pagerState.currentPage)
    }

    val viewingTx by transactionsViewModel.viewingTransaction.collectAsStateWithLifecycle()
    viewingTx?.let { tx ->
        TransactionDetailSheet(transaction = tx) {
            transactionsViewModel.closeTransactionDetails()
        }
    }

    val healthScore by statsViewModel.healthScore.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {

        DynamicWeatherOverlay(score = healthScore)

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding() - innerPadding.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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

                        HealthBadge(score = healthScore)
                    }
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (uiState.currencyData.isNotEmpty()) {
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 32.dp),
                                pageSpacing = 16.dp,
                                beyondViewportPageCount = 1,
                                modifier = Modifier.fillMaxWidth()
                            ) { pageIndex ->
                                val currencyStats = uiState.currencyData.getOrNull(pageIndex)
                                if (currencyStats != null) {
                                    val symbol =
                                        CurrencyMapper.getSymbol(currencyStats.currencyCode)

                                    val isTotalCard = pageIndex == 0

                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            val pageOffset =
                                                ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                            scaleY = lerp(
                                                start = 0.9f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                        }
                                    ) {
                                        BalanceFlowCard(
                                            stats = currencyStats,
                                            symbol = symbol,
                                            isTotal = isTotalCard
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    PredictionCoin(
                        prediction = prediction,
                        isFlipped = isFlipped,
                        onFlip = { settingsViewModel.flipCoin() }
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(enabled = !isAiLoading) {
                                aiViewModel.fetchAdvice(currentUserName, budgets)
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
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
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val displayMessage = when {
                                        isAiLoading && aiAdvice.isEmpty() -> stringResource(R.string.ai_loading)
                                        aiAdvice == "ERROR_STATE" -> stringResource(R.string.ai_error_internet)
                                        aiAdvice.isEmpty() -> stringResource(R.string.ai_click_prompt)
                                        else -> aiAdvice
                                    }

                                    Text(
                                        text = displayMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
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
                            Text(
                                text = stringResource(R.string.home_dream_goal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.Dream.route) },
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.4f
                                    )
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { progressData.progress },
                                            modifier = Modifier.size(64.dp),
                                            strokeCap = StrokeCap.Round,
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.1f
                                            )
                                        )
                                        Text(text = progressData.dream.iconEmoji, fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = progressData.dream.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.dream_collected_percentage,
                                                (progressData.progress * 100).toInt()
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                val critical =
                    budgets.filter { it.progress > 0.6f }.sortedByDescending { it.progress }
                if (critical.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = stringResource(R.string.home_critical_limits),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.home_all_limits),
                                    modifier = Modifier.clickable { onNavigateToBudgets() },
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = stringResource(R.string.home_recent_tx),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.home_view_all),
                                modifier = Modifier.clickable { onNavigateToTransactions() },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val lastThree = transactions.take(3)
                        if (lastThree.isEmpty()) {
                            Text(
                                text = stringResource(R.string.home_no_transactions),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            lastThree.forEach { tx ->
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { transactionsViewModel.showTransactionDetails(tx) },
                                    onLongClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}