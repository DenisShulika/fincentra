    package com.denisshulika.fincentra.ui.screens

    import android.graphics.drawable.Icon
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

    @Composable
    fun TransactionsScreen(
        viewModel: TransactionsViewModel
    ) {
        val list = viewModel.transactions.collectAsStateWithLifecycle().value

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.addTestTransaction() }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Додати")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                if (list.isEmpty()) {
                    Text(text = "Транзакцій ще немає", modifier = Modifier.align(Alignment.Center))
                } else {
                    Text(text = "Знайдено транзакцій: ${list.size}")
                }
            }
        }
    }