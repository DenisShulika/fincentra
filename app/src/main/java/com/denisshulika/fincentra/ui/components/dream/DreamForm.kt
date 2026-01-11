package com.denisshulika.fincentra.ui.components.dream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DreamForm(
    title: String,
    onTitleChange: (String) -> Unit,
    target: String,
    onTargetChange: (String) -> Unit,
    buffer: String,
    onBufferChange: (String) -> Unit,
    isLoading: Boolean,
    isExistingDream: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val isFormValid = title.isNotBlank() && target.isNotBlank() && buffer.isNotBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isExistingDream) "Оновити параметри" else "Налаштувати мрію",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("На що збираємо?") },
            placeholder = { Text("Наприклад: Автомобіль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = target,
            onValueChange = onTargetChange,
            label = { Text("Сума цілі") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = buffer,
            onValueChange = onBufferChange,
            label = { Text("Поріг безпеки (на життя)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !isLoading && isFormValid,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isExistingDream) "Оновити мрію" else "Зберегти мрію",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (isExistingDream) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Скасувати", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}