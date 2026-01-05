package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.ui.components.SwipeBackground
import com.denisshulika.fincentra.ui.components.TransactionDetailSheet
import com.denisshulika.fincentra.ui.components.TransactionItem
import com.denisshulika.fincentra.ui.components.transactions.CategoryFilterContent
import com.denisshulika.fincentra.ui.components.transactions.DateRangePickerDialog
import com.denisshulika.fincentra.ui.components.transactions.TransactionFormContent
import com.denisshulika.fincentra.ui.components.transactions.TransactionsTopBar
import com.denisshulika.fincentra.ui.components.transactions.TypeBankFilterContent
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

    val viewingTx by viewModel.viewingTransaction.collectAsStateWithLifecycle()
    viewingTx?.let { tx ->
        TransactionDetailSheet(transaction = tx) {
            viewModel.closeTransactionDetails()
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Видалити транзакцію?") },
            text = { Text("Ви впевнені? Цю дію не можна буде скасувати.") },
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
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Скасувати")
                }
            }
        )
    }

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            modifier = Modifier.size(64.dp),
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Транзакцій за такими фільтрами немає",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.toggleSearch(false) }) {
                            Text("Скинути всі фільтри")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(list, key = { it.id + it.timestamp }) { tx ->
                        val isManual = tx.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL

                        if (isManual) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            transactionToDelete = tx
                                            false
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            viewModel.prepareForEdit(tx)
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = { SwipeBackground(dismissState) },
                                modifier = Modifier.animateItem()
                            ) {
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { viewModel.showTransactionDetails(tx) },
                                    onLongClick = {
                                        transactionToDelete = tx
                                    }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.animateItem()) {
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { viewModel.showTransactionDetails(tx) },
                                    onLongClick = { }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}