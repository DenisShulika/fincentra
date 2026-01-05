package com.denisshulika.fincentra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.network.common.CurrencyMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = transaction.category.color.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = transaction.category.materialIcon,
                    contentDescription = null,
                    tint = transaction.category.color,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val symbol = CurrencyMapper.getSymbol(transaction.currencyCode)
            Text(
                text = "${if (transaction.isExpense) "-" else "+"}${transaction.amount} $symbol",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) Color(0xFFE57373) else Color(0xFF81C784)
            )

            Text(
                text = transaction.description.ifBlank { "Без опису" },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailRow("Категорія", transaction.category.displayName)
            DetailRow("Підкатегорія", transaction.subCategoryName)
            DetailRow("Джерело", transaction.bankName)
            DetailRow("Дата", com.denisshulika.fincentra.data.util.DateFormatter.dateTime.format(java.util.Date(transaction.timestamp)))

            transaction.mcc?.let { DetailRow("MCC код", it.toString()) }
            transaction.comment?.let { DetailRow("Коментар банку", it) }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}