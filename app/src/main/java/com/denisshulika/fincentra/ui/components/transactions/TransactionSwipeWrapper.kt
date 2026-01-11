package com.denisshulika.fincentra.ui.components.transactions

import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.TransactionConstants
import com.denisshulika.fincentra.ui.components.TransactionItem
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun TransactionSwipeWrapper(
    transaction: Transaction,
    viewModel: TransactionsViewModel,
    onDeleteRequest: () -> Unit
) {
    val isManual = transaction.accountId == TransactionConstants.ACCOUNT_ID_MANUAL

    if (isManual) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDeleteRequest()
                        false
                    }

                    SwipeToDismissBoxValue.StartToEnd -> {
                        viewModel.prepareForEdit(transaction)
                        false
                    }

                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = { SwipeBackground(dismissState) }
        ) {
            TransactionItem(
                transaction = transaction,
                onClick = { viewModel.showTransactionDetails(transaction) },
                onLongClick = { onDeleteRequest() }
            )
        }
    } else {
        TransactionItem(
            transaction = transaction,
            onClick = { viewModel.showTransactionDetails(transaction) },
            onLongClick = { }
        )
    }
}