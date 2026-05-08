package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.util.DateFormatter
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.transactions.DateRangePickerDialog
import com.denisshulika.fincentra.viewmodels.ExportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onBack: () -> Unit
) {
    val sources by viewModel.availableSources.collectAsStateWithLifecycle()
    val selectedSources by viewModel.selectedSources.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    val dateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()

    val includeHeader by viewModel.includeHeader.collectAsStateWithLifecycle()
    val includeSummary by viewModel.includeSummary.collectAsStateWithLifecycle()

    val accounts by viewModel.availableAccounts.collectAsStateWithLifecycle()
    val selectedAccountIds by viewModel.selectedAccountIds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FinCentraTopBar(stringResource(R.string.export_screen_title), false, onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.export_screen_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(20.dp))

            ExportSectionTitle(stringResource(R.string.export_screen_section_dates))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (dateRange == null) stringResource(R.string.export_screen_date_all_time)
                    else "${DateFormatter.formatFullDate(dateRange!!.first)} - ${
                        DateFormatter.formatFullDate(
                            dateRange!!.last
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            ExportSectionTitle(stringResource(R.string.export_screen_section_sources))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sources.forEach { source ->
                    FilterChip(
                        selected = selectedSources.contains(source),
                        onClick = { viewModel.toggleSource(source) },
                        label = { Text(source, fontSize = 12.sp) },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.export_screen_section_accounts),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedAccountIds.isEmpty(),
                        onClick = { viewModel.clearAccountIds() },
                        label = { Text(stringResource(R.string.export_screen_all_cards)) },
                        shape = CircleShape
                    )
                }

                items(accounts.size) { index ->
                    val acc = accounts[index]
                    FilterChip(
                        selected = selectedAccountIds.contains(acc.id),
                        onClick = { viewModel.toggleAccountId(acc.id) },
                        label = { Text(acc.name) },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val currencies by viewModel.availableCurrencies.collectAsStateWithLifecycle()
            val selectedCurrencies by viewModel.selectedCurrencies.collectAsStateWithLifecycle()

            Text(
                stringResource(R.string.export_screen_section_currencies),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCurrencies.isEmpty(),
                        onClick = { viewModel.clearCurrencies() },
                        label = { Text(stringResource(R.string.export_screen_all_currencies)) },
                        shape = CircleShape
                    )
                }

                items(currencies.size) { index ->
                    val code = currencies[index]
                    FilterChip(
                        selected = selectedCurrencies.contains(code),
                        onClick = { viewModel.toggleCurrency(code) },
                        label = {
                            Text(
                                CurrencyMapper.getCodeName(
                                    code
                                )
                            )
                        },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExportSectionTitle(
                    stringResource(
                        R.string.export_screen_section_categories,
                        selectedCategories.size
                    )
                )
                IconButton(onClick = { isCategoriesExpanded = !isCategoriesExpanded }) {
                    Icon(
                        imageVector = if (isCategoriesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = isCategoriesExpanded) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    TransactionCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategories.contains(cat.name),
                            onClick = { viewModel.toggleCategory(cat.name) },
                            label = { Text(stringResource(cat.displayNameRes), fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    cat.materialIcon,
                                    null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = CircleShape,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ExportSectionTitle(stringResource(R.string.export_screen_section_type))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    FilterConstants.ALL,
                    FilterConstants.EXPENSES,
                    FilterConstants.INCOME
                ).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { viewModel.setType(type) },
                        label = {
                            Text(
                                text = when (type) {
                                    FilterConstants.EXPENSES -> stringResource(R.string.export_screen_type_expenses)
                                    FilterConstants.INCOME -> stringResource(R.string.export_screen_type_income)
                                    else -> stringResource(R.string.export_screen_type_all)
                                },
                                fontSize = 12.sp
                            )
                        },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.export_screen_section_style),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.export_screen_style_header),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = includeHeader,
                            onCheckedChange = viewModel::setIncludeHeader
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.export_screen_style_summary),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = includeSummary,
                            onCheckedChange = viewModel::setIncludeSummary
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.shareFile(context, isPdf = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = transactions.isNotEmpty()
            ) {
                Text(
                    stringResource(R.string.export_screen_btn_pdf, transactions.size),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.shareFile(context, isPdf = false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = transactions.isNotEmpty()
            ) {
                Text(stringResource(R.string.export_screen_btn_csv), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) viewModel.setDateRange(start..end)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun ExportSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}