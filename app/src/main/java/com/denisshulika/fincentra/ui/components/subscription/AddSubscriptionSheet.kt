package com.denisshulika.fincentra.ui.components.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.SubFrequency
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.util.DateFormatter
import com.denisshulika.fincentra.viewmodels.SubscriptionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionSheet(viewModel: SubscriptionViewModel, onDismiss: () -> Unit) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val name by viewModel.name.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val selectedFreq by viewModel.selectedFrequency.collectAsStateWithLifecycle()
    val editingId by viewModel.editingSubId.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val selectedDateByVm by viewModel.selectedDate.collectAsStateWithLifecycle()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateByVm
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.add_subscription_sheet_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.add_subscription_sheet_date_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(onDismissRequest = {
        onDismiss()
        viewModel.resetForm()
    }) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (editingId == null) stringResource(R.string.add_subscription_sheet_title_new) else stringResource(
                    R.string.add_subscription_sheet_title_details
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name, onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.add_subscription_sheet_label_service_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount, onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.add_subscription_sheet_label_amount)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = {
                    Text(
                        text = CurrencyMapper.getSymbol(selectedCurrency),
                        modifier = Modifier.clickable {
                            val next = when (selectedCurrency) {
                                980 -> 840; 840 -> 978; else -> 980
                            }
                            viewModel.onCurrencyChange(next)
                        },
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.add_subscription_sheet_billing_cycle),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubFrequency.entries.forEach { freq ->
                    FilterChip(
                        selected = selectedFreq == freq,
                        onClick = { viewModel.onFrequencyChange(freq) },
                        label = {
                            val label = when (freq) {
                                SubFrequency.WEEKLY -> stringResource(R.string.add_subscription_sheet_freq_weekly)
                                SubFrequency.MONTHLY -> stringResource(R.string.add_subscription_sheet_freq_monthly)
                                SubFrequency.YEARLY -> stringResource(R.string.add_subscription_sheet_freq_yearly)
                            }
                            Text(label)
                        },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.DateRange, null)
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        stringResource(R.string.add_subscription_sheet_label_payment_date),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = DateFormatter.formatFullDate(selectedDateByVm),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveSubscription()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = name.isNotBlank() && amount.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
                else Text(
                    if (editingId == null) stringResource(R.string.add_subscription_sheet_btn_add) else stringResource(
                        R.string.add_subscription_sheet_btn_save
                    ),
                    fontWeight = FontWeight.Bold
                )
            }

            if (editingId != null) {
                TextButton(
                    onClick = {
                        viewModel.deleteSubscription(editingId!!)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.add_subscription_sheet_btn_cancel_sub),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
