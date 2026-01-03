package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.Transaction
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