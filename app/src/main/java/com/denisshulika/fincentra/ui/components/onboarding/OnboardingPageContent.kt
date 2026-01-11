package com.denisshulika.fincentra.ui.components.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.fincentra.R

@Composable
fun OnboardingPageContent(
    page: Int,
    isCurrentPage: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (page) {
            0 -> {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(180.dp),
                        shape = RoundedCornerShape(40.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {}
                    Image(
                        painter = painterResource(id = R.drawable.fincentra_logo),
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Розумна аналітика",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append("FinCentra ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("автоматично")
                        }
                        append(" збирає дані з твоїх банків. Більше ніяких блокнотів — просто спостерігай за ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("результатом.")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            1 -> {
                TreeAnimationBlock(isVisible = isCurrentPage)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Твоя екосистема",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Твій ліміт — це ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("живе дерево")
                        }
                        append(". Контролюй витрати, щоб твій фінансовий ліс завжди залишався ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("зеленим та квітучим.")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            2 -> {
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🎯",
                                fontSize = 90.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Мрій без ризику",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Ми автоматично ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("захистимо гроші на життя")
                        }
                        append(", а надлишок спрямуємо до мети. Досягай мрій, зберігаючи ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("фінансовий спокій.")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}