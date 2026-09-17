package com.denisshulika.fincentra.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.PrefDreamEmoji
import com.denisshulika.fincentra.data.util.PrefDreamProgress
import com.denisshulika.fincentra.data.util.PrefDreamTitle

class DreamWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val prefs = currentState<Preferences>()

            val title = prefs[PrefDreamTitle] ?: context.getString(R.string.dream_widget_no_dream)
            val progress = prefs[PrefDreamProgress] ?: 0f
            val emoji = prefs[PrefDreamEmoji] ?: "🚀"

            val progressBitmap = createCircularProgressBitmap(context, progress)

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF041E13))
                        .cornerRadius(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = GlanceModifier.size(100.dp)
                        ) {
                            Image(
                                provider = ImageProvider(progressBitmap),
                                contentDescription = null,
                                modifier = GlanceModifier.fillMaxSize()
                            )
                            Text(
                                text = emoji,
                                style = TextStyle(
                                    fontSize = 45.sp,
                                    color = ColorProvider(Color.White)
                                )
                            )
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            text = title.uppercase(),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = context.getString(
                                R.string.dream_widget_progress_percent,
                                (progress * 100).toInt()
                            ),
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }

    private fun createCircularProgressBitmap(context: Context, progress: Float): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeWidth = 16f
        val margin = strokeWidth / 2
        val rect = RectF(margin, margin, size - margin, size - margin)

        val trackPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 40
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawOval(rect, trackPaint)

        val progressPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#22C55E")
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        val sweepAngle = (progress.coerceIn(0f, 1f) * 360f)
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        return bitmap
    }
}

class DreamWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DreamWidget()
}