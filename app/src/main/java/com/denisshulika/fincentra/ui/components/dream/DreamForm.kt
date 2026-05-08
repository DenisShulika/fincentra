package com.denisshulika.fincentra.ui.components.dream

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.network.common.CurrencyMapper

@Composable
fun DreamForm(
    title: String,
    onTitleChange: (String) -> Unit,
    target: String,
    onTargetChange: (String) -> Unit,
    buffer: String,
    onBufferChange: (String) -> Unit,
    emoji: String,
    onEmojiChange: (String) -> Unit,
    selectedCurrency: Int,
    onCurrencyChange: (Int) -> Unit,
    isLoading: Boolean,
    isExistingDream: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val isFormValid = title.isNotBlank() && target.isNotBlank() && buffer.isNotBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isExistingDream) stringResource(R.string.dream_form_title_edit)
            else stringResource(R.string.dream_form_title_new),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = emoji,
                onValueChange = {
                    if (it.length <= 2) onEmojiChange(it)
                },
                label = { Text(stringResource(R.string.dream_form_label_icon)) },
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center)
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.dream_form_label_title)) },
                placeholder = { Text(stringResource(R.string.dream_form_placeholder_title)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        OutlinedTextField(
            value = target,
            onValueChange = onTargetChange,
            label = { Text(stringResource(R.string.dream_form_label_target)) },
            suffix = {
                Text(
                    text = CurrencyMapper.getSymbol(selectedCurrency),
                    modifier = Modifier.clickable {
                        val next = when (selectedCurrency) {
                            980 -> 840
                            840 -> 978
                            978 -> 946
                            946 -> 985
                            else -> 980
                        }
                        onCurrencyChange(next)
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = buffer,
            onValueChange = onBufferChange,
            label = { Text(stringResource(R.string.dream_form_label_buffer)) },
            suffix = {
                Text(
                    text = CurrencyMapper.getSymbol(selectedCurrency),
                    modifier = Modifier.clickable {
                        val next = when (selectedCurrency) {
                            980 -> 840
                            840 -> 978
                            978 -> 946
                            946 -> 985
                            else -> 980
                        }
                        onCurrencyChange(next)
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !isLoading && isFormValid,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
            else Text(
                if (isExistingDream) stringResource(R.string.dream_form_btn_update)
                else stringResource(R.string.dream_form_btn_save),
                fontWeight = FontWeight.Bold
            )
        }

        if (isExistingDream) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.dream_form_btn_delete), fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.dream_form_btn_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}