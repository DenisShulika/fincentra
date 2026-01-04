package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.CategoryStat
import com.denisshulika.fincentra.data.models.CurrencyStats
import com.denisshulika.fincentra.viewmodels.StatsViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedCurrencyIndex.collectAsStateWithLifecycle()

    val currentStats = uiState.currencyData.getOrNull(selectedIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Фінансовий звіт",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        if (currentStats != null) {
            val symbol = com.denisshulika.fincentra.data.network.common.CurrencyMapper.getSymbol(currentStats.currencyCode)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    BalanceFlowCard(currentStats, symbol)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text(
                        text = "Розподіл витрат",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (currentStats.categories.isEmpty()) {
                    item {
                        Text(
                            text = "Витрат у цій валюті не знайдено",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(currentStats.categories) { stat ->
                        CategoryStatItem(stat = stat, symbol = symbol)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Транзакцій не знайдено.\nСинхронізуйте дані у вкладці 'Банки'.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BalanceFlowCard(stats: CurrencyStats, symbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("На початок періоду", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${String.format("%.2f", stats.startPeriodBalance)} $symbol", style = MaterialTheme.typography.bodyMedium)
            }

            val netChange = stats.totalIncome - stats.totalExpense
            val changeColor = if (netChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

            Row(
                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Рух коштів (Net)", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = (if (netChange >= 0) "+" else "") + "${String.format("%.2f", netChange)} $symbol",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Поточний баланс", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = "${String.format("%.2f", stats.endPeriodBalance)} $symbol",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryStatRow(stat: CategoryStat, symbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stat.category.displayName)
        Text("${String.format("%.2f", stat.amount)} $symbol")
    }
}

@Composable
fun CategoryStatItem(stat: CategoryStat, symbol: String) {
    var isExpanded by remember { mutableStateOf(false) }

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
                Text(text = stat.category.displayName, style = MaterialTheme.typography.bodyLarge)
            }

            Text(
                text = "${String.format("%.2f", stat.amount)} $symbol",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.percentage)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(stat.category.color)
            )
        }

        Text(
            text = "${(stat.percentage * 100).toInt()}% від усіх витрат",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
        )

        if (isExpanded && stat.subCategories.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 44.dp, top = 8.dp)
                    .fillMaxWidth()
            ) {
                stat.subCategories.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${String.format("%.2f", sub.amount)} $symbol",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { sub.percentageOfParent },
                        modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                        color = stat.category.color.copy(alpha = 0.5f),
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}