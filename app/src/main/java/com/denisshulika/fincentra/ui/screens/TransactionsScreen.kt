    package com.denisshulika.fincentra.ui.screens

    import android.graphics.drawable.Icon
    import android.util.Log
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.LazyRow
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.lazy.layout.LazyLayout
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.Button
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.FilterChip
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.ModalBottomSheet
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.SegmentedButton
    import androidx.compose.material3.SegmentedButtonDefaults
    import androidx.compose.material3.SingleChoiceSegmentedButtonRow
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextButton
    import androidx.compose.material3.rememberModalBottomSheetState
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableIntStateOf
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import com.denisshulika.fincentra.data.models.Transaction
    import com.denisshulika.fincentra.ui.components.TransactionItem
    import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransactionsScreen(
        viewModel: TransactionsViewModel
    ) {
        val list = viewModel.transactions.collectAsStateWithLifecycle().value

        val sheetState = rememberModalBottomSheetState()

        val amount by viewModel.amount.collectAsStateWithLifecycle()
        val description by viewModel.description.collectAsStateWithLifecycle()
        val showBottomSheet by viewModel.showBottomSheet.collectAsStateWithLifecycle()
        val isExpense by viewModel.isExpense.collectAsStateWithLifecycle()
        val category by viewModel.category.collectAsStateWithLifecycle()

        val expenseOptions = viewModel.expenseOptions

        var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

        transactionToDelete?.let { transaction ->
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text("Видалити транзакцію?") },
                text = { Text("Ви впевнені, що хочете видалити '${transaction.description}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(transaction)
                            transactionToDelete = null
                        }
                    ) { Text("Видалити", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToDelete = null }) { Text("Скасувати") }
                }
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleBottomSheet(false) },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нова транзакція", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.onAmountChange(it) },
                        label = { Text("Сума") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        label = { Text("Опис") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Категорія", style = MaterialTheme.typography.labelLarge)
                    LazyRow (
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(viewModel.categories) { cat ->
                            FilterChip(
                                selected = (category == cat),
                                onClick = { viewModel.onCategoryChange(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        expenseOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = expenseOptions.size
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.addTransaction() },
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        enabled = amount.isNotBlank()
                    ) {
                        Text("Зберегти")
                    }
                }
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.toggleBottomSheet(true) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val sortedList = list.sortedByDescending { it.timestamp }

                items(sortedList) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onLongClick = { transactionToDelete = transaction }
                    )
                }
            }
        }
    }