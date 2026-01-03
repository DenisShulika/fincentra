package com.denisshulika.fincentra.ui.components.transactions

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun CategoryFilterContent(
    viewModel: TransactionsViewModel,
    onDismiss: () -> Unit
) {
    val selectedCats by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val unfilteredTx by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categories = viewModel.categories

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Фільтр за категоріями",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(categories) { mainCat ->
                val subCategories = remember(mainCat) {
                    com.denisshulika.fincentra.data.network.common.MccDirectory.getSubcategoriesFor(mainCat)
                }
                var isExpanded by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCats.contains(mainCat.displayName),
                            onCheckedChange = {
                                viewModel.toggleCategoryFilter(mainCat.displayName)
                            }
                        )

                        Text(
                            text = mainCat.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )

                        if (subCategories.isNotEmpty()) {
                            IconButton(
                                onClick = { isExpanded = !isExpanded }
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Розгорнути",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        subCategories.forEach { subName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp)
                                    .clickable {
                                        viewModel.toggleCategoryFilter(subName)
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCats.contains(subName),
                                    onCheckedChange = {
                                        viewModel.toggleCategoryFilter(subName)
                                    }
                                )
                                Text(
                                    text = subName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Застосувати")
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
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Сортування",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TransactionsViewModel.SortOrder.entries) { order ->
                FilterChip(
                    selected = (selectedSort == order),
                    onClick = {
                        viewModel.onSortOrderChange(order)
                    },
                    label = {
                        Text(order.displayName)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Тип операції",
            style = MaterialTheme.typography.titleMedium
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            listOf("Всі", "Витрати", "Доходи").forEach { type ->
                FilterChip(
                    selected = (selectedType == type),
                    onClick = {
                        viewModel.onTypeFilterChange(type)
                    },
                    label = {
                        Text(type)
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Джерело",
            style = MaterialTheme.typography.titleMedium
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            listOf("Всі", "Monobank", "Готівка").forEach { bank ->
                FilterChip(
                    selected = (selectedBank == bank),
                    onClick = {
                        viewModel.onBankFilterChange(bank)
                    },
                    label = {
                        Text(bank)
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text("Готово")
        }
    }
}