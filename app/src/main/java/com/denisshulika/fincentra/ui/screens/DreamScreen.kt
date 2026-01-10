package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.viewmodels.DreamViewModel

@Composable
fun DreamScreen(
    viewModel: DreamViewModel,
    onBack: () -> Unit
) {
    val progressState by viewModel.dreamProgress.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var isEditMode by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var buffer by remember { mutableStateOf("") }

    LaunchedEffect(progressState) {
        progressState?.let { state ->
            title = state.dream.title
            target = state.dream.targetAmount.toInt().toString()
            buffer = state.dream.safetyBuffer.toInt().toString()
        }
    }

    Scaffold(
        topBar = {
            FinCentraTopBar(
                title = "Моя мрія",
                isTopLevelScreen = false,
                onNavigationClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoCard()

            Spacer(modifier = Modifier.height(24.dp))

            if (progressState == null || isEditMode) {
                DreamForm(
                    title = title,
                    onTitleChange = { title = it },
                    target = target,
                    onTargetChange = { target = it },
                    buffer = buffer,
                    onBufferChange = { buffer = it },
                    isLoading = isLoading,
                    isExistingDream = progressState != null,
                    onSave = {
                        viewModel.updateDream(
                            title,
                            target.toDoubleOrNull() ?: 0.0,
                            buffer.toDoubleOrNull() ?: 0.0
                        )
                        isEditMode = false
                    },
                    onCancel = { if (progressState != null) isEditMode = false }
                )
            } else {
                progressState?.let { data ->
                    DreamProgressView(
                        progressData = data,
                        onEditClick = { isEditMode = true }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFBC02D),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Як це працює?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "🎯 Мрія",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Додаток автоматично підраховує твої реальні гроші на всіх підключених картках. Тобі не треба вносити накопичення вручну — ми бачимо твій прогрес у реальному часі.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "🛡️ Поріг безпеки",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Це твій «недоторканний запас» на їжу, оренду та побут. Прогрес мрії рахується тільки з «вільних» коштів, що перевищують цю суму. Це гарантує, що ти не залишишся з порожнім гаманцем заради мети.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DreamProgressView(progressData: DreamProgress, onEditClick: () -> Unit) {
    val symbol = "₴"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progressData.progress },
                modifier = Modifier.size(150.dp),
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(text = progressData.dream.iconEmoji, style = MaterialTheme.typography.displayLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = progressData.dream.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ПРАВИЛЬНЕ ОКРУГЛЕННЯ ЧЕРЕЗ %.2f
                DetailRow(
                    "Ціль",
                    "${String.format("%.2f", progressData.dream.targetAmount)} $symbol"
                )
                DetailRow(
                    "Поріг безпеки",
                    "-${String.format("%.2f", progressData.dream.safetyBuffer)} $symbol"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    "Доступно зараз",
                    "${String.format("%.2f", progressData.currentAvailable)} $symbol",
                    isHighlight = true
                )
            }
        }

        TextButton(onClick = onEditClick, modifier = Modifier.padding(top = 16.dp)) {
            Text("Редагувати ціль")
        }
    }
}

@Composable
fun DreamForm(
    title: String, onTitleChange: (String) -> Unit,
    target: String, onTargetChange: (String) -> Unit,
    buffer: String, onBufferChange: (String) -> Unit,
    isLoading: Boolean,
    isExistingDream: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val isFormValid = title.isNotBlank() && target.isNotBlank() && buffer.isNotBlank()

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("На що збираємо?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = target,
            onValueChange = onTargetChange,
            label = { Text("Сума мрії") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = buffer,
            onValueChange = onBufferChange,
            label = { Text("Поріг безпеки (на життя)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isFormValid
        ) {
            Text("Зберегти мрію")
        }

        if (isExistingDream) {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Скасувати")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}