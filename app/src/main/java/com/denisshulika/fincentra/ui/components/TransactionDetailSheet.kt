package com.denisshulika.fincentra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(transaction: Transaction, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HERO SECTION
            Surface(
                shape = CircleShape,
                color = transaction.category.color.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = transaction.category.materialIcon,
                    contentDescription = null,
                    tint = transaction.category.color,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val symbol = CurrencyMapper.getSymbol(transaction.currencyCode)
            Text(
                text = "${if (transaction.isExpense) "-" else "+"}${
                    String.format(
                        "%.2f",
                        transaction.amount
                    )
                } $symbol",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (transaction.isExpense) MaterialTheme.colorScheme.error else Color(
                    0xFF4CAF50
                )
            )

            Text(
                text = transaction.description.ifBlank { "Операція без назви" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Категорія", transaction.category.displayName)
                DetailRow("Підкатегорія", transaction.subCategoryName)
                DetailRow("Джерело", transaction.bankName)
                DetailRow(
                    "Час операції",
                    DateFormatter.formatDateTime(transaction.timestamp)
                )

                if (transaction.comment != null) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Text(
                        "Коментар банку",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(transaction.comment, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Закрити", fontWeight = FontWeight.Bold)
            }
        }
    }
}