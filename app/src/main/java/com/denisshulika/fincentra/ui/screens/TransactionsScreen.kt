    package com.denisshulika.fincentra.ui.screens

    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

    @Composable
    fun TransactionsScreen(
        viewModel: TransactionsViewModel
    ) {
        val list = viewModel.transactions.collectAsStateWithLifecycle().value

        Text("Знайдено транзакцій: ${list.size}")
    }