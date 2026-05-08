package com.denisshulika.fincentra.ui.components.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.state.CategoryStat
import com.denisshulika.fincentra.ui.components.AnimatedAmount

@Composable
fun SpendingDonutChart(categories: List<CategoryStat>, symbol: String, isExpenseMode: Boolean) {
    val totalAmount = categories.sumOf { it.amount }
    val animatedTotal by animateFloatAsState(
        targetValue = totalAmount.toFloat(),
        animationSpec = tween(1000),
        label = "Total"
    )

    Box(modifier = Modifier.size(230.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            var startAngle = -90f

            if (totalAmount == 0.0) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val gap = if (categories.size > 1) 4f else 0f
                val availableAngle = 360f - (gap * categories.size)

                categories.forEach { stat ->
                    val sweep = (stat.amount / totalAmount).toFloat() * availableAngle
                    drawArc(
                        color = stat.category.color,
                        startAngle = startAngle, sweepAngle = sweep, useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep + gap
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isExpenseMode)
                    stringResource(R.string.spending_donut_chart_expenses)
                else
                    stringResource(R.string.spending_donut_chart_income),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedAmount(
                targetAmount = totalAmount,
                symbol = symbol,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )
        }
    }
}