package com.denisshulika.fincentra.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.fincentra.R

@Composable
fun PredictionCoin(
    prediction: String,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "CoinRotation"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(85.dp)
            .combinedClickable(
                enabled = !isFlipped,
                onClick = onFlip,
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A2E1F).copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coin),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color(0xFF16A34A).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF22C55E).copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                        ) {
                            Text("✨", fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!isFlipped) stringResource(R.string.prediction_coin_title_default) else prediction,
                    style = if (!isFlipped) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
                    fontWeight = if (!isFlipped) FontWeight.Black else FontWeight.Bold,
                    color = if (!isFlipped) Color.White else Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
                if (!isFlipped) {
                    Text(
                        text = stringResource(R.string.prediction_coin_tap_instruction),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF22C55E).copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}