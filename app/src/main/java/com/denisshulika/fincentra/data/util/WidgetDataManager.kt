package com.denisshulika.fincentra.data.util

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.ui.widgets.DreamWidget
import com.denisshulika.fincentra.ui.widgets.TreeWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

val PrefDreamTitle = stringPreferencesKey(WidgetConstants.KEY_DREAM_TITLE)
val PrefDreamProgress = floatPreferencesKey(WidgetConstants.KEY_DREAM_PROGRESS)
val PrefDreamEmoji = stringPreferencesKey(WidgetConstants.KEY_DREAM_EMOJI)

class WidgetDataManager(private val context: Context) {
    private val scope = MainScope()

    fun saveDreamData(progress: DreamProgress?) {
        scope.launch {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(DreamWidget::class.java)

            ids.forEach { id ->
                updateAppWidgetState(context, id) { prefs ->
                    prefs[PrefDreamTitle] = progress?.dream?.title ?: "No Dream"
                    prefs[PrefDreamProgress] = progress?.progress ?: 0f
                    prefs[PrefDreamEmoji] = progress?.dream?.iconEmoji ?: "🚀"
                }
                DreamWidget().update(context, id)
            }
        }
    }

    fun saveAllBudgets(list: List<BudgetProgress>) {
        val dataString = list.joinToString(";") {
            "${it.budget.categoryName},${it.progress},${it.treeImageRes}"
        }
        context.getSharedPreferences(WidgetConstants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("all_budgets_data", dataString)
            .apply()

        scope.launch {
            TreeWidget().updateAll(context)
        }
    }
}