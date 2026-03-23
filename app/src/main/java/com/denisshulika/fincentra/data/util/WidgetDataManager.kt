package com.denisshulika.fincentra.data.util

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.ui.widgets.DreamWidget
import com.denisshulika.fincentra.ui.widgets.TreeWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetDataManager(private val context: Context) {
    private val prefs =
        context.getSharedPreferences(WidgetConstants.PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = MainScope()

    fun saveDreamData(progress: DreamProgress?) {
        prefs.edit().apply {
            putString(WidgetConstants.KEY_DREAM_TITLE, progress?.dream?.title ?: "No Dream")
            putFloat(WidgetConstants.KEY_DREAM_PROGRESS, progress?.progress ?: 0f)
            putString(WidgetConstants.KEY_DREAM_EMOJI, progress?.dream?.iconEmoji ?: "🚀")
            apply()
        }
        scope.launch { DreamWidget().updateAll(context) }
    }

    fun saveAllBudgets(list: List<BudgetProgress>) {
        val dataString = list.joinToString(";") {
            "${it.budget.categoryName},${it.progress},${it.treeImageRes}"
        }
        prefs.edit().putString("all_budgets_data", dataString).apply()

        scope.launch { TreeWidget().updateAll(context) }
    }
}