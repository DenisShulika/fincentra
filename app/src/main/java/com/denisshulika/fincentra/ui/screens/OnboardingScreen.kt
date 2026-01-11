package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.fincentra.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(
                page = page,
                isCurrentPage = pagerState.currentPage == page
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onFinish) {
                Text("Пропустити", color = Color.Gray)
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (pagerState.currentPage == 2) "Почати" else "Далі")
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: Int, isCurrentPage: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (page) {
            0 -> {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        Modifier.size(160.dp),
                        shape = RoundedCornerShape(30),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {}
                    Image(
                        painter = painterResource(id = R.drawable.fincentra_logo),
                        modifier = Modifier
                            .size(128.dp)
                            .clip(RoundedCornerShape(30)),
                        contentDescription = null
                    )
                }
                Spacer(Modifier.height(48.dp))
                Text(
                    "Розумна аналітика",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
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
                    style = textStyle
                )
            }

            1 -> {
                TreeAnimationBlock(isVisible = isCurrentPage)
                Spacer(Modifier.height(20.dp))
                Text(
                    "Твоя екосистема",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
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
                    style = textStyle
                )
            }

            2 -> {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🎯",
                                style = TextStyle(fontSize = 80.sp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
                Text(
                    "Мрій без ризику",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
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
                    style = textStyle
                )
            }
        }
    }
}

@Composable
fun TreeAnimationBlock(isVisible: Boolean) {
    val treeStages = listOf(
        R.drawable.img_tree_dead,
        R.drawable.img_tree_warning,
        R.drawable.img_tree_medium,
        R.drawable.img_tree_healthy
    )

    var currentIndex by remember { mutableIntStateOf(0) }

    val transitionSpeed = 700
    val stayDuration = 1000L

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                for (i in 0 until treeStages.size) {
                    currentIndex = i
                    delay(stayDuration)
                }

                delay(500)

                for (i in (treeStages.size - 2) downTo 1) {
                    currentIndex = i
                    delay(stayDuration)
                }
            }
        } else {
            currentIndex = 0
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(250.dp)
    ) {
        Crossfade(
            targetState = treeStages[currentIndex],
            animationSpec = tween(durationMillis = transitionSpeed),
            label = "SnappyTreeCycle"
        ) { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .padding(10.dp)
            )
        }
    }
}