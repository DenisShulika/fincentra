package com.denisshulika.fincentra.ui.components.dream

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.data.network.common.CurrencyMapper

@Composable
fun DreamProgressView(progressData: DreamProgress, onEditClick: () -> Unit) {
    val symbol = CurrencyMapper.getSymbol(progressData.dream.currencyCode)

    val animatedProgress by animateFloatAsState(
        targetValue = progressData.progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "DreamProgress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(200.dp),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = progressData.dream.iconEmoji,
                fontSize = 80.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = progressData.dream.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                DetailRow(
                    label = stringResource(R.string.dream_progress_view_label_target),
                    value = stringResource(
                        R.string.dream_progress_view_amount_format,
                        progressData.dream.targetAmount,
                        symbol
                    )
                )
                DetailRow(
                    label = stringResource(R.string.dream_progress_view_label_buffer),
                    value = stringResource(
                        R.string.dream_progress_view_amount_buffer_format,
                        progressData.dream.safetyBuffer,
                        symbol
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                DetailRow(
                    label = stringResource(R.string.dream_progress_view_label_available),
                    value = stringResource(
                        R.string.dream_progress_view_amount_format,
                        progressData.currentAvailable,
                        symbol
                    ),
                    isHighlight = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(R.string.dream_progress_view_btn_edit),
                fontWeight = FontWeight.Bold
            )
        }
    }
}