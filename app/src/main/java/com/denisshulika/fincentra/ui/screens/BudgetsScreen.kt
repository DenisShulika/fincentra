package com.denisshulika.fincentra.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Додати")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "У вас ще немає лімітів.\nНатисніть +, щоб додати перше дерево.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
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

@Composable
fun BudgetProgressItem(
    item: BudgetProgress,
    onClick: () -> Unit
) {
    val symbol = CurrencyMapper.getSymbol(item.budget.currencyCode)

    val statusColor = when {
        item.progress <= 0.5f -> Color(0xFF4CAF50)
        item.progress <= 0.85f -> Color(0xFFFFB300)
        item.progress < 1.0f -> Color(0xFFFF5722)
        else -> Color(0xFFB71C1C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.treeImageRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.budget.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.0f", item.remainingAmount)} $symbol",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.progress >= 1f) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "залишилось",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = item.progress,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "BudgetProgress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(viewModel: BudgetsViewModel) {
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = { viewModel.toggleAddSheet(false) }) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Встановити ліміт на місяць", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Сума ліміту") },
                suffix = { Text(CurrencyMapper.getSymbol(selectedCurrency)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Категорія", modifier = Modifier.align(Alignment.Start))
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TransactionCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveNewBudget() },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank()
            ) {
                Text("Зберегти ліміт")
            }
        }
    }
}