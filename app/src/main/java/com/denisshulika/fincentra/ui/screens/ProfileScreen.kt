package com.denisshulika.fincentra.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.models.state.CurrencySummary
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.viewmodels.BudgetsViewModel
import com.denisshulika.fincentra.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    budgetsViewModel: BudgetsViewModel,
    onLogout: () -> Unit
) {
    val showAddBudget by budgetsViewModel.showAddSheet.collectAsStateWithLifecycle()
    val summaries by viewModel.currencySummaries.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val txCount by viewModel.totalTransactionsCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val provider by viewModel.provider.collectAsStateWithLifecycle()

    if (showAddBudget) {
        AddBudgetSheet(viewModel = budgetsViewModel)
    }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Зміна пароля") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Новий пароль") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.changePassword(newPassword) { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    showPasswordDialog = false
                    newPassword = ""
                }) { Text("Оновити") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Видалити акаунт?") },
            text = { Text("Всі ваші дані в хмарі будуть втрачені. Цю дію не можна скасувати.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(
                        onDeleted = onLogout,
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                    )
                }) { Text("Видалити", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Скасувати") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (user?.photoUrl?.isNotEmpty() == true) {
                    Text(
                        text = user?.displayName?.take(1) ?: user?.email?.take(1) ?: "?",
                        style = MaterialTheme.typography.headlineLarge
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.displayName?.ifBlank { "Користувач" } ?: "Завантаження...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = user?.email ?: "", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        SuggestionChip(
            onClick = { },
            label = { Text("$txCount транзакцій") },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(16.dp)) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (summaries.isNotEmpty()) {
                items(summaries) { summary ->
                    BalanceCard(summary)
                }
            } else {
                item {
                    Text(
                        "Рахунки не підключені",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                BudgetSection(
                    viewModel = budgetsViewModel,
                    onAddClick = { budgetsViewModel.toggleAddSheet(true) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = { context.startActivity(viewModel.getSupportIntent()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Зв'язатися з розробником")
            }

            Text(
                text = "Керування акаунтом",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (provider == "password") {
                    IconButton(onClick = { showPasswordDialog = true }) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color.Red)
                }
                IconButton(onClick = { viewModel.logout(onLogout) }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BalanceCard(summary: CurrencySummary) {
    val symbol = CurrencyMapper.getSymbol(summary.currencyCode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Загальний баланс ($symbol)", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${String.format("%.2f", summary.balance)} $symbol",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BudgetSection(
    viewModel: BudgetsViewModel,
    onAddClick: () -> Unit
) {
    val budgets by viewModel.budgetProgressList.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Мої ліміти", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Додати ліміт")
            }
        }

        if (budgets.isEmpty()) {
            Text("Ліміти ще не встановлені", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            budgets.forEach { item ->
                BudgetProgressItem(
                    item = item,
                    onClick = {
                        viewModel.prepareForEdit(item.budget)
                    }
                )
                Spacer(Modifier.height(12.dp))
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
            LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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