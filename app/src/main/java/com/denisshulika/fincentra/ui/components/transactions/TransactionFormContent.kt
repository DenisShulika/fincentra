package com.denisshulika.fincentra.ui.components.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun TransactionFormContent(viewModel: TransactionsViewModel) {
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val isExpense by viewModel.isExpense.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val editingId by viewModel.editingTransactionId.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (editingId == null) stringResource(R.string.transaction_new_title) else stringResource(
                R.string.transaction_edit_title
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text(stringResource(R.string.transaction_label_amount)) },
            suffix = {
                Text(
                    text = CurrencyMapper.getSymbol(selectedCurrency),
                    modifier = Modifier.clickable {
                        val next = when (selectedCurrency) {
                            980 -> 840
                            840 -> 978
                            else -> 980
                        }
                        viewModel.onCurrencyChange(next)
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text(stringResource(R.string.transaction_label_desc)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.tx_category_label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.categories.forEach { cat ->
                FilterChip(
                    selected = (category == cat),
                    onClick = { viewModel.onCategoryChange(cat) },
                    label = {
                        Text(
                            text = stringResource(cat.displayNameRes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            viewModel.expenseOptions.forEachIndexed { index, resId ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = viewModel.expenseOptions.size
                    ),
                    onClick = { viewModel.onTypeChange(index == 0) },
                    selected = if (index == 0) isExpense else !isExpense,
                    label = {
                        Text(
                            text = stringResource(resId),
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.saveTransaction() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = amount.isNotBlank()
        ) {
            Text(
                text = if (editingId == null) stringResource(R.string.transaction_btn_save) else stringResource(
                    R.string.transaction_btn_update
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}