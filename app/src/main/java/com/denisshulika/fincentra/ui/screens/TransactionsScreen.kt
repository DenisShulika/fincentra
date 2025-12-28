    package com.denisshulika.fincentra.ui.screens

    import android.graphics.drawable.Icon
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material3.Button
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.ModalBottomSheet
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.rememberModalBottomSheetState
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
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