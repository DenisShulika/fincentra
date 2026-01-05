package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.CategoryStat
import com.denisshulika.fincentra.data.models.CurrencyStats
import com.denisshulika.fincentra.data.models.StatsPeriod
import com.denisshulika.fincentra.ui.components.AnimatedAmount
import com.denisshulika.fincentra.ui.components.transactions.DateRangePickerDialog
import com.denisshulika.fincentra.viewmodels.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedCurrencyIndex.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    val currentStats = uiState.currencyData.getOrNull(selectedIndex)
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    viewModel.setCustomDateRange(start..end)
                }
                showDatePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Фінансовий звіт",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (uiState.currencyData.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                uiState.currencyData.forEachIndexed { index, data ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.selectCurrency(index) },
                        text = {
                            Text(com.denisshulika.fincentra.data.network.common.CurrencyMapper.getCodeName(data.currencyCode))
                        }
                    )
                }
            }
        }

        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { viewModel.setPeriod(it) },
            onCalendarClick = { showDatePicker = true }
        )

        if (currentStats != null) {
            val symbol = com.denisshulika.fincentra.data.network.common.CurrencyMapper.getSymbol(currentStats.currencyCode)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    BalanceFlowCard(currentStats, symbol)
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpendingDonutChart(categories = currentStats.categories, symbol = symbol)
                    }
                }

                item {
                    Text(
                        text = "Розподіл витрат",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(currentStats.categories) { stat ->
                    CategoryStatItem(stat = stat, symbol = symbol)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Транзакцій не знайдено.\nСинхронізуйте дані у вкладці 'Банки'.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BalanceFlowCard(stats: CurrencyStats, symbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "На початок періоду",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedAmount(
                    targetAmount = stats.startPeriodBalance,
                    symbol = symbol,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val netChange = stats.totalIncome - stats.totalExpense
            val changeColor = if (netChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

            Row(
                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Рух коштів (Net)", style = MaterialTheme.typography.titleMedium)
                AnimatedAmount(
                    targetAmount = netChange,
                    symbol = symbol,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = changeColor
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Поточний баланс", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                AnimatedAmount(
                    targetAmount = stats.endPeriodBalance,
                    symbol = symbol,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryStatItem(stat: CategoryStat, symbol: String) {
    var isExpanded by remember { mutableStateOf(false) }

    // Анімація прогресу
    val animatedProgress by animateFloatAsState(
        targetValue = stat.percentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = stat.category.color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = stat.category.materialIcon,
                        contentDescription = null,
                        tint = stat.category.color,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = stat.category.displayName, style = MaterialTheme.typography.bodyLarge)
                    if (stat.subCategories.isNotEmpty()) {
                        Text(
                            text = "${stat.subCategories.size} підкатегорій",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AnimatedAmount(
                    targetAmount = stat.amount,
                    symbol = symbol,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(stat.category.color)
            )
        }

        if (isExpanded && stat.subCategories.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 44.dp, top = 12.dp)
                    .fillMaxWidth()
            ) {
                stat.subCategories.forEach { sub ->
                    val subProgress by animateFloatAsState(
                        targetValue = sub.percentageOfParent,
                        animationSpec = tween(durationMillis = 1000),
                        label = "SubProgress"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedAmount(
                            targetAmount = sub.amount,
                            symbol = symbol,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { subProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(bottom = 4.dp),
                        color = stat.category.color.copy(alpha = 0.5f),
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingDonutChart(
    categories: List<CategoryStat>,
    symbol: String,
    modifier: Modifier = Modifier
) {
    val totalExpense = categories.sumOf { it.amount }

    val animatedTotal by animateFloatAsState(
        targetValue = totalExpense.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "TotalAnimation"
    )

    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            var startAngle = -90f

            if (totalExpense == 0.0) {
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val gapAngle = if (categories.size > 1) 3f else 0f
                val totalGapAngle = gapAngle * categories.size
                val availableAngle = 360f - totalGapAngle

                categories.forEach { stat ->
                    val sweepAngle = (stat.amount / totalExpense).toFloat() * availableAngle

                    drawArc(
                        color = stat.category.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle + gapAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Витрати",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = "${String.format("%.0f", animatedTotal)} $symbol",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onCalendarClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(listOf(StatsPeriod.WEEK, StatsPeriod.MONTH, StatsPeriod.QUARTER, StatsPeriod.ALL)) { period ->
            FilterChip(
                selected = (selectedPeriod == period),
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) }
            )
        }

        item {
            FilterChip(
                selected = (selectedPeriod == StatsPeriod.CUSTOM),
                onClick = onCalendarClick,
                label = { Icon(Icons.Default.DateRange, null, Modifier.size(18.dp)) },
                leadingIcon = { if (selectedPeriod == StatsPeriod.CUSTOM) Icon(Icons.Default.Check, null, Modifier.size(14.dp)) else null }
            )
        }
    }
}