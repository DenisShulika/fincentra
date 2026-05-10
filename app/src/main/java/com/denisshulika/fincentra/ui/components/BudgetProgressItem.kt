package com.denisshulika.fincentra.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.network.common.CurrencyMapper

@Composable
fun BudgetProgressItem(
    item: BudgetProgress,
    onClick: () -> Unit
) {
    val symbol = CurrencyMapper.getSymbol(item.budget.currencyCode)

    val statusColor = when {
        item.progress <= 0.5f -> MaterialTheme.colorScheme.primary
        item.progress < 1.0f -> Color(0xFFFFB300)
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.treeImageRes),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val category =
                        TransactionCategory.entries.find { it.name == item.budget.categoryName }
                    Text(
                        text = category?.let { stringResource(it.displayNameRes) }
                            ?: item.budget.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(item.statusRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val remaining = item.remainingAmount
                    val total = item.budget.limitAmount

                    Text(
                        text = stringResource(
                            R.string.budget_progress_item_remaining,
                            remaining,
                            symbol
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (item.progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(
                            R.string.budget_progress_item_limit_info,
                            total.toInt(),
                            symbol
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = item.progress.coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                ),
                label = "Progress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}