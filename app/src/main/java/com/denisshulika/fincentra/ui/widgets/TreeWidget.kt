package com.denisshulika.fincentra.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
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
import com.denisshulika.fincentra.data.util.PrefAllBudgetsData
import com.denisshulika.fincentra.data.util.WidgetConstants

val PrefSelectedCat = stringPreferencesKey("selected_cat")
val PrefIsSelecting = booleanPreferencesKey("is_selecting")
val CategoryKey = ActionParameters.Key<String>("category_id_key")

class TreeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isSelectionMode = prefs[PrefIsSelecting] ?: true
            val selectedCategory = prefs[PrefSelectedCat]

            val globalPrefs =
                context.getSharedPreferences(WidgetConstants.PREFS_NAME, Context.MODE_PRIVATE)
            val allData =
                prefs[PrefAllBudgetsData] ?: globalPrefs.getString("all_budgets_data", "") ?: ""

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF041E13))
                        .cornerRadius(28.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelectionMode || selectedCategory == null) {
                        SelectionView(context, allData)
                    } else {
                        val budget = allData.split(";").mapNotNull {
                            val parts = it.split(",")
                            if (parts.size == 3 && parts[0] == selectedCategory) parts else null
                        }.firstOrNull()

                        if (budget != null) {
                            TreeView(context, budget[0], budget[1].toFloat(), budget[2].toInt())
                        } else {
                            SelectionView(context, allData)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SelectionView(context: Context, allData: String) {
        val categories = allData.split(";").filter { it.isNotBlank() }

        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = context.getString(R.string.tree_widget_selection_header),
                modifier = GlanceModifier.padding(bottom = 10.dp).fillMaxWidth(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )

            if (categories.isEmpty()) {
                Box(
                    modifier = GlanceModifier.padding(horizontal = 8.dp).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        context.getString(R.string.tree_widget_no_data_hint),
                        style = TextStyle(
                            color = ColorProvider(Color.Gray),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(categories) { catRow ->
                        val name = catRow.split(",")[0]
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF16A34A))
                                .cornerRadius(12.dp)
                                .clickable(
                                    actionRunCallback<SelectCategoryCallback>(
                                        actionParametersOf(CategoryKey to name)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TreeView(context: Context, name: String, progress: Float, imageRes: Int) {
        val neonGreen = Color(0xFF22C55E)
        val displayColor = if (progress >= 1f) Color.Red else neonGreen

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionRunCallback<EnterSelectionModeCallback>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(imageRes),
                contentDescription = null,
                modifier = GlanceModifier.size(100.dp)
            )

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = name,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )

            Spacer(GlanceModifier.height(10.dp))

            Box(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = GlanceModifier.fillMaxWidth().height(14.dp),
                    color = ColorProvider(displayColor),
                    backgroundColor = ColorProvider(Color.White.copy(alpha = 0.15f))
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            Text(
                text = context.getString(
                    R.string.tree_widget_usage_status,
                    (progress * 100).toInt()
                ),
                style = TextStyle(
                    color = ColorProvider(displayColor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

class SelectCategoryCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val cat = parameters[CategoryKey] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PrefSelectedCat] = cat
            prefs[PrefIsSelecting] = false
        }
        TreeWidget().update(context, glanceId)
    }
}

class EnterSelectionModeCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PrefIsSelecting] = true
        }
        TreeWidget().update(context, glanceId)
    }
}

class TreeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TreeWidget()
}