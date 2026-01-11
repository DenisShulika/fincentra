package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.ui.components.BudgetProgressItem
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.budgets.AddBudgetSheet
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel

@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel,
    onBack: () -> Unit
) {
    val budgets by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val showAddSheet by viewModel.showAddSheet.collectAsStateWithLifecycle()

    if (showAddSheet) {
        AddBudgetSheet(viewModel = viewModel)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinCentraTopBar(
                title = "Ліміти витрат",
                isTopLevelScreen = false,
                onNavigationClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NaturePeople,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Твій ліс поки порожній",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Встанови ліміт на категорію, щоб почати вирощувати своє фінансове дерево.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(budgets) { item ->
                        BudgetProgressItem(
                            item = item,
                            onClick = { viewModel.prepareForEdit(item.budget) }
                        )
                    }
                }
            }
        }
    }
}