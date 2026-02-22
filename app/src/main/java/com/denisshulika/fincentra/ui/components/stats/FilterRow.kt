package com.denisshulika.fincentra.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.data.util.TransactionConstants

@Composable
fun FilterRow(
    selectedBank: String,
    selectedAccountId: String?,
    availableAccounts: List<com.denisshulika.fincentra.data.models.domain.BankAccount>,
    onBankChange: (String) -> Unit,
    onAccountChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val banks = listOf(
                FilterConstants.ALL to R.string.filter_all,
                BankProviders.MONOBANK to R.string.filter_monobank,
                TransactionConstants.SOURCE_CASH to R.string.filter_cash
            )

            items(banks) { (technicalName, labelRes) ->
                FilterChip(
                    selected = selectedBank == technicalName,
                    onClick = { onBankChange(technicalName) },
                    label = { Text(stringResource(labelRes)) },
                    shape = CircleShape
                )
            }
        }

        if (selectedBank != "Всі") {
            val filtered = availableAccounts.filter { it.provider == selectedBank && it.selected }
            if (filtered.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedAccountId == null,
                            onClick = { onAccountChange(null) },
                            label = { Text("Всі карти") },
                            shape = CircleShape
                        )
                    }
                    items(filtered) { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { onAccountChange(acc.id) },
                            label = { Text(acc.name) },
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}