package com.denisshulika.fincentra.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedAmount(
    targetAmount: Double,
    symbol: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    var isVisible by remember { mutableStateOf(false) }

    val animatedValue by animateFloatAsState(
        targetValue = if (isVisible) targetAmount.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "AmountAnimation"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Text(
        text = "${String.format("%.2f", animatedValue)} $symbol",
        style = style,
        color = color,
        modifier = modifier
    )
}