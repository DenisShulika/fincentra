package com.denisshulika.fincentra.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.denisshulika.fincentra.R
import kotlin.random.Random

data class FinancialParticle(
    val x: Float,
    val initialY: Float,
    val speedMultiplier: Float,
    val size: Float,
    val rotation: Float,
    val opacity: Float
)

@Composable
fun DynamicWeatherOverlay(score: Int) {
    val coinImg = ImageBitmap.imageResource(id = R.drawable.ic_particle_coin)
    val greenLeafImg = ImageBitmap.imageResource(id = R.drawable.ic_particle_leaf_green)
    val dryLeafImg = ImageBitmap.imageResource(id = R.drawable.ic_particle_leaf_dry)

    val currentImage = when {
        score > 80 -> coinImg
        score in 50..80 -> greenLeafImg
        else -> dryLeafImg
    }

    val particles = remember(currentImage) {
        List(18) {
            FinancialParticle(
                x = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speedMultiplier = Random.nextInt(1, 4).toFloat(),
                size = Random.nextFloat() * 30f + 40f,
                rotation = Random.nextFloat() * 360f,
                opacity = Random.nextFloat() * 0.2f + 0.06f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weather")
    val timeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val currentYProgress = (p.initialY + (timeProgress * p.speedMultiplier)) % 1f

            val xPos = p.x * width
            val yPos = currentYProgress * (height + 200f) - 100f
            val currentSize = p.size.toInt()

            val horizontalOffset = if (score <= 80) {
                kotlin.math.sin(timeProgress * 20f + p.initialY * 100f) * 15f
            } else 0f

            rotate(p.rotation, pivot = Offset(xPos + horizontalOffset, yPos)) {
                drawImage(
                    image = currentImage,
                    dstOffset = IntOffset(
                        (xPos + horizontalOffset - currentSize / 2).toInt(),
                        (yPos - currentSize / 2).toInt()
                    ),
                    dstSize = IntSize(currentSize, currentSize),
                    alpha = p.opacity
                )
            }
        }
    }
}