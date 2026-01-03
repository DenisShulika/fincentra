package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.models.TransactionCategory
import com.denisshulika.fincentra.ui.components.TransactionItem
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel) {
    val list by viewModel.transactions.collectAsStateWithLifecycle()
    val showBottomSheet by viewModel.showBottomSheet.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showTypeBankSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Діалог видалення
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                transactionToDelete = null
            },
            title = {
                Text("Видалити транзакцію?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    }
                ) {
                    Text("Видалити", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        transactionToDelete = null
                    }
                ) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Modal Sheets
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleBottomSheet(false) },
            sheetState = sheetState
        ) {
            TransactionFormContent(viewModel)
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            CategoryFilterContent(viewModel) {
                showFilterSheet = false
            }
        }
    }

    if (showTypeBankSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTypeBankSheet = false }
        ) {
            TypeBankFilterContent(viewModel) {
                showTypeBankSheet = false
            }
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = {
                showDatePicker = false
            },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    viewModel.setDateRange(start..end)
                }
                showDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TransactionsTopBar(
                viewModel = viewModel,
                onFilterCategoryClick = { showFilterSheet = true },
                onFilterTypeClick = { showTypeBankSheet = true },
                onFilterDateClick = { showDatePicker = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleBottomSheet(true) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нічого не знайдено", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(list, key = { it.id + it.timestamp }) { tx ->
                        TransactionItem(
                            transaction = tx,
                            onClick = { viewModel.prepareForEdit(tx) },
                            onLongClick = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }
}

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

@Composable
fun TransactionFormContent(viewModel: TransactionsViewModel) {
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val isExpense by viewModel.isExpense.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val editingId by viewModel.editingTransactionId.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (editingId == null) "Нова транзакція" else "Редагування",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Сума") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Опис") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Категорія", style = MaterialTheme.typography.labelLarge)

        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.categories) { cat ->
                FilterChip(
                    selected = (category == cat),
                    onClick = {
                        viewModel.onCategoryChange(cat)
                    },
                    label = {
                        Text(cat.displayName)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            viewModel.expenseOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = viewModel.expenseOptions.size
                    ),
                    onClick = {
                        viewModel.onTypeChange(index == 0)
                    },
                    selected = if (index == 0) isExpense else !isExpense
                ) {
                    Text(label)
                }
            }
        }

        Button(
            onClick = {
                viewModel.saveTransaction()
            },
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            enabled = amount.isNotBlank()
        ) {
            Text(if (editingId == null) "Зберегти" else "Оновити")
        }
    }
}

@Composable
fun TransactionsTopBar(
    viewModel: TransactionsViewModel,
    onFilterCategoryClick: () -> Unit,
    onFilterTypeClick: () -> Unit,
    onFilterDateClick: () -> Unit
) {
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isSearchActive) {
            Text(
                text = "Транзакції",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(
                onClick = { viewModel.toggleSearch(true) }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Пошук")
            }
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFilterCategoryClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "Категорії")
                }
                IconButton(onClick = onFilterTypeClick) {
                    Icon(Icons.Default.Tune, contentDescription = "Банк/Тип")
                }
                IconButton(onClick = onFilterDateClick) {
                    Icon(Icons.Default.DateRange, contentDescription = "Дати")
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Пошук...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            IconButton(
                onClick = { viewModel.toggleSearch(false) }
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрити")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showModeToggle = true,
            title = {
                Text(
                    text = "Виберіть період",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            headline = {
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                val start = state.selectedStartDateMillis?.let { sdf.format(it) } ?: "ДД.ММ.РРРР"
                val end = state.selectedEndDateMillis?.let { sdf.format(it) } ?: "ДД.ММ.РРРР"

                Text(
                    text = "$start — $end",
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        )
    }
}