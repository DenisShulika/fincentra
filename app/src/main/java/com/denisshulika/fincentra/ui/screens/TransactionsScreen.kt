package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.ui.components.TransactionDetailSheet
import com.denisshulika.fincentra.ui.components.transactions.CategoryFilterContent
import com.denisshulika.fincentra.ui.components.transactions.DateRangePickerDialog
import com.denisshulika.fincentra.ui.components.transactions.EmptyTransactionsPlaceholder
import com.denisshulika.fincentra.ui.components.transactions.TransactionFormContent
import com.denisshulika.fincentra.ui.components.transactions.TransactionSwipeWrapper
import com.denisshulika.fincentra.ui.components.transactions.TransactionsTopBar
import com.denisshulika.fincentra.ui.components.transactions.TypeBankFilterContent
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel, onOpenDrawer: () -> Unit) {
    val list by viewModel.transactions.collectAsStateWithLifecycle()
    val showBottomSheet by viewModel.showBottomSheet.collectAsStateWithLifecycle()
    val groupedList by viewModel.groupedTransactions.collectAsStateWithLifecycle()
    val viewingTx by viewModel.viewingTransaction.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showTypeBankSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    viewingTx?.let { tx -> TransactionDetailSheet(transaction = tx) { viewModel.closeTransactionDetails() } }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    stringResource(R.string.tx_delete_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.tx_delete_confirm_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transactionToDelete!!); transactionToDelete = null
                }) {
                    Text(
                        stringResource(R.string.btn_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(
                        stringResource(R.string.btn_cancel)
                    )
                }
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { viewModel.toggleBottomSheet(false) }) {
            TransactionFormContent(
                viewModel
            )
        }
    }
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            CategoryFilterContent(
                viewModel
            ) { showFilterSheet = false }
        }
    }
    if (showTypeBankSheet) {
        ModalBottomSheet(onDismissRequest = { showTypeBankSheet = false }) {
            TypeBankFilterContent(
                viewModel
            ) { showTypeBankSheet = false }
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
            })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransactionsTopBar(
                viewModel,
                onFilterCategoryClick = { showFilterSheet = true },
                onOpenDrawer = onOpenDrawer,
                onFilterTypeClick = { showTypeBankSheet = true },
                onFilterDateClick = { showDatePicker = true })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleBottomSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (list.isEmpty()) {
                EmptyTransactionsPlaceholder { viewModel.toggleSearch(false) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedList.forEach { (dateHeader, transactions) ->
                        stickyHeader {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                            ) {
                                val displayDate = when (dateHeader) {
                                    "DATE_TODAY" -> stringResource(R.string.date_today)
                                    "DATE_YESTERDAY" -> stringResource(R.string.date_yesterday)
                                    else -> dateHeader
                                }
                                Text(
                                    text = displayDate.uppercase(),
                                    modifier = Modifier.padding(
                                        horizontal = 20.dp,
                                        vertical = 12.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        items(transactions, key = { it.id + it.timestamp }) { tx ->
                            TransactionSwipeWrapper(
                                transaction = tx,
                                viewModel = viewModel,
                                onDeleteRequest = { transactionToDelete = tx })
                        }
                    }
                }
            }
        }
    }
}