package com.denisshulika.fincentra.ui.components.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.data.util.TransactionConstants
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun CategoryFilterContent(
    viewModel: TransactionsViewModel,
    onDismiss: () -> Unit
) {
    val selectedCats by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val categoriesMap by viewModel.categoriesWithSubs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.filter_categories_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categoriesMap.keys.toList()) { mainCat ->
                val subCategories = categoriesMap[mainCat] ?: emptyList()
                var isExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCats.contains(mainCat.name),
                            onCheckedChange = { viewModel.toggleCategoryFilter(mainCat.name) }
                        )
                        Text(
                            text = stringResource(mainCat.displayNameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (subCategories.isNotEmpty()) {
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        subCategories.forEach { subResId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 44.dp)
                                    .clickable { viewModel.toggleCategoryFilter(subResId.toString()) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCats.contains(subResId.toString()),
                                    onCheckedChange = { viewModel.toggleCategoryFilter(subResId.toString()) }
                                )
                                Text(
                                    text = stringResource(subResId),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.filter_btn_apply), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TypeBankFilterContent(
    viewModel: TransactionsViewModel,
    onDismiss: () -> Unit
) {
    val selectedType by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedBank by viewModel.selectedBankFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSortOrder.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.filter_appearance_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.filter_sort_by),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TransactionsViewModel.SortOrder.entries) { order ->
                FilterChip(
                    selected = (selectedSort == order),
                    onClick = { viewModel.onSortOrderChange(order) },
                    label = { Text(stringResource(order.displayNameRes)) },
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.filter_type),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            val types = listOf(
                FilterConstants.ALL to R.string.filter_all,
                FilterConstants.EXPENSES to R.string.stats_expenses,
                FilterConstants.INCOME to R.string.stats_income
            )
            types.forEach { (tech, res) ->
                FilterChip(
                    selected = (selectedType == tech),
                    onClick = { viewModel.onTypeFilterChange(tech) },
                    label = { Text(stringResource(res)) },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.filter_source),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            val sources = listOf(
                FilterConstants.ALL to R.string.filter_all,
                BankProviders.MONOBANK to R.string.filter_monobank,
                TransactionConstants.SOURCE_CASH to R.string.filter_cash
            )
            sources.forEach { (tech, res) ->
                FilterChip(
                    selected = (selectedBank == tech),
                    onClick = { viewModel.onBankFilterChange(tech) },
                    label = { Text(stringResource(res)) },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = CircleShape
                )
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.filter_btn_done), fontWeight = FontWeight.Bold)
        }
    }
}