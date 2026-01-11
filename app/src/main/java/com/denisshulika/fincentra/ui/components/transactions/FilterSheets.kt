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
    val categoriesMap by viewModel.categoriesWithSubs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Фільтр категорій",
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
                            checked = selectedCats.contains(mainCat.displayName),
                            onCheckedChange = { viewModel.toggleCategoryFilter(mainCat.displayName) }
                        )
                        Text(
                            text = mainCat.displayName,
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
                        subCategories.forEach { subName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 44.dp)
                                    .clickable { viewModel.toggleCategoryFilter(subName) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCats.contains(subName),
                                    onCheckedChange = { viewModel.toggleCategoryFilter(subName) }
                                )
                                Text(
                                    text = subName,
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
            Text("Застосувати", fontWeight = FontWeight.Bold)
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
            text = "Налаштування вигляду",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Сортування за",
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
                    label = { Text(order.displayName) },
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Тип операції",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            listOf("Всі", "Витрати", "Доходи").forEach { type ->
                FilterChip(
                    selected = (selectedType == type),
                    onClick = { viewModel.onTypeFilterChange(type) },
                    label = { Text(type) },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Джерело",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            listOf("Всі", "Monobank", "Готівка").forEach { bank ->
                FilterChip(
                    selected = (selectedBank == bank),
                    onClick = { viewModel.onBankFilterChange(bank) },
                    label = { Text(bank) },
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
            Text("Готово", fontWeight = FontWeight.Bold)
        }
    }
}