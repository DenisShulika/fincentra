package com.denisshulika.fincentra.ui.components.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import kotlinx.coroutines.delay

@Composable
fun TreeAnimationBlock(
    isVisible: Boolean
) {
    val treeStages = listOf(
        R.drawable.img_tree_dead,
        R.drawable.img_tree_healthy,
        R.drawable.img_tree_money
    )

    var currentIndex by remember {
        mutableIntStateOf(0)
    }

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

                currentIndex = 1
                delay(stayDuration)
            }
        } else {
            currentIndex = 0
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {
        Crossfade(
            targetState = treeStages[currentIndex],
            animationSpec = tween(durationMillis = transitionSpeed),
            label = "TreeEvolution"
        ) { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(12.dp)
            )
        }
    }
}