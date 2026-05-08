package com.denisshulika.fincentra.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.fincentra.R

@Composable
fun HealthBadge(score: Int) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500),
        label = "ScoreAnimation"
    )

    val badgeColor by animateColorAsState(
        targetValue = when {
            score >= 80 -> Color(0xFF22C55E)
            score >= 50 -> Color(0xFFFACC15)
            else -> Color(0xFFEF4444)
        },
        label = "ColorAnimation"
    )

    Surface(
        modifier = Modifier.size(56.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = badgeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(2.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$animatedScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = badgeColor,
                    lineHeight = 16.sp
                )
                Text(
                    text = stringResource(R.string.health_badge_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}