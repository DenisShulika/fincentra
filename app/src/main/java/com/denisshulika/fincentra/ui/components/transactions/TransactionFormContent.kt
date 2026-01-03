package com.denisshulika.fincentra.ui.components.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

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
