package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.ui.components.BalanceFlowCard
import com.denisshulika.fincentra.ui.components.stats.CategoryStatItem
import com.denisshulika.fincentra.ui.components.stats.EmptyStatsPlaceholder
import com.denisshulika.fincentra.ui.components.stats.FilterRow
import com.denisshulika.fincentra.ui.components.stats.PeriodSelector
import com.denisshulika.fincentra.ui.components.stats.SpendingDonutChart
import com.denisshulika.fincentra.ui.components.transactions.DateRangePickerDialog
import com.denisshulika.fincentra.viewmodels.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedCurrencyIndex.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val isExpenseMode by viewModel.isExpenseMode.collectAsStateWithLifecycle()
    val selectedBank by viewModel.selectedBank.collectAsStateWithLifecycle()
    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()
    val availableAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var isFiltersExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) viewModel.setCustomDateRange(start..end)
                showDatePicker = false
            })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                FilterChip(
                    selected = isExpenseMode,
                    onClick = { viewModel.toggleMode(true) },
                    label = { Text(stringResource(R.string.stats_expenses)) },
                    shape = CircleShape
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = !isExpenseMode,
                    onClick = { viewModel.toggleMode(false) },
                    label = { Text(stringResource(R.string.stats_income)) },
                    shape = CircleShape
                )
            }
            IconButton(onClick = {
                isFiltersExpanded = !isFiltersExpanded
            }) {
                Icon(
                    if (isFiltersExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        AnimatedVisibility(visible = isFiltersExpanded) {
            Column {
                if (uiState.currencyData.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}) {
                        uiState.currencyData.forEachIndexed { index, data ->
                            Tab(
                                selected = selectedIndex == index,
                                onClick = { viewModel.selectCurrency(index) },
                                text = { Text(CurrencyMapper.getCodeName(data.currencyCode)) })
                        }
                    }
                }
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    onCalendarClick = { showDatePicker = true })
                FilterRow(
                    selectedBank = selectedBank,
                    selectedAccountId = selectedAccountId,
                    availableAccounts = availableAccounts,
                    onBankChange = { viewModel.onBankFilterChange(it) },
                    onAccountChange = { viewModel.onAccountFilterChange(it) })
            }
        }
        val currentStats = uiState.currencyData.getOrNull(selectedIndex)
        if (currentStats != null) {
            val symbol = CurrencyMapper.getSymbol(currentStats.currencyCode)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                item { BalanceFlowCard(currentStats, symbol) }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { SpendingDonutChart(currentStats.categories, symbol, isExpenseMode) }
                }
                item {
                    Text(
                        text = if (isExpenseMode) stringResource(R.string.stats_expense_distribution) else stringResource(
                            R.string.stats_income_sources
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
                items(currentStats.categories) { stat ->
                    CategoryStatItem(
                        stat = stat,
                        symbol = symbol
                    )
                }
            }
        } else {
            EmptyStatsPlaceholder()
        }
    }
}