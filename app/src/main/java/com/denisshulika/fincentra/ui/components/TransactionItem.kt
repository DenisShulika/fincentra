package com.denisshulika.fincentra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.DateFormatter
import com.denisshulika.fincentra.data.util.TransactionConstants

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    color = transaction.category.color.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = transaction.category.materialIcon,
                        contentDescription = null,
                        tint = transaction.category.color,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 4.dp, y = 4.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    if (transaction.bankName == BankProviders.MONOBANK) {
                        Image(
                            painter = painterResource(id = R.drawable.monobank_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = transaction.description.ifBlank { transaction.category.displayName },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (transaction.subCategoryName == TransactionConstants.DEFAULT_SUB_CATEGORY)
                        transaction.category.displayName else transaction.subCategoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val symbol = CurrencyMapper.getSymbol(transaction.currencyCode)
                val color =
                    if (transaction.isExpense) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)

                Text(
                    text = "${if (transaction.isExpense) "-" else "+"}${
                        String.format(
                            "%.2f",
                            transaction.amount
                        )
                    } $symbol",
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = DateFormatter.timeOnly.format(java.util.Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}