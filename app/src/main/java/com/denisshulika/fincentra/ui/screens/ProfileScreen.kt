package com.denisshulika.fincentra.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.profile.BalanceCard
import com.denisshulika.fincentra.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onBack: () -> Unit, onLogout: () -> Unit) {
    val summaries by viewModel.currencySummaries.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val txCount by viewModel.totalTransactionsCount.collectAsStateWithLifecycle()
    val provider by viewModel.provider.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.profile_new_password_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.profile_enter_password_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(
                            onDeleted = onLogout,
                            onError = { key ->
                                val res = context.resources
                                val msg = if (key == "ERROR_REAUTH")
                                    res.getString(R.string.error_relogin_required)
                                else res.getString(R.string.error_unknown)
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.btn_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.profile_delete_acc_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.profile_delete_acc_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.changePassword(newPassword) { key ->
                            val res = context.resources
                            val msg = when (key) {
                                "SUCCESS_PASSWORD" -> res.getString(R.string.success_password_changed)
                                "ERROR_REAUTH" -> res.getString(R.string.error_relogin_required)
                                else -> res.getString(R.string.error_unknown)
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.btn_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinCentraTopBar(
                title = stringResource(R.string.profile_title_screen),
                isTopLevelScreen = false,
                onNavigationClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user?.displayName?.take(1) ?: user?.email?.take(1) ?: "?",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = user?.displayName?.ifBlank { stringResource(R.string.profile_user_placeholder) }
                ?: stringResource(R.string.profile_loading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black)
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        stringResource(R.string.profile_transactions_count, txCount),
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(20.dp)) },
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                if (summaries.isNotEmpty()) {
                    items(summaries) { BalanceCard(it) }
                } else {
                    item {
                        Text(
                            stringResource(R.string.profile_no_accounts),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = { context.startActivity(viewModel.getSupportIntent()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Email,
                        null,
                        Modifier.size(20.dp)
                    ); Spacer(Modifier.width(12.dp)); Text(
                    stringResource(R.string.profile_contact_dev),
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.profile_account_actions),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    if (provider == "password") {
                        FilledTonalIconButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Icon(Icons.Default.Lock, null) }
                    }
                    FilledTonalIconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Icon(Icons.Default.DeleteForever, null) }
                    Button(
                        onClick = { viewModel.logout(onLogout) },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            null
                        ); Spacer(Modifier.width(8.dp)); Text(
                        stringResource(R.string.btn_logout),
                        fontWeight = FontWeight.Bold
                    )
                    }
                }
            }
        }
    }
}