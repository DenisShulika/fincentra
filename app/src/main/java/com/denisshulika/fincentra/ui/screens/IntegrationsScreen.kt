package com.denisshulika.fincentra.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.viewmodels.IntegrationsViewModel

@Composable
fun IntegrationsScreen(viewModel: IntegrationsViewModel) {
    val isConnected by viewModel.isBankConnected.collectAsStateWithLifecycle()
    val isInputVisible by viewModel.isMonoInputVisible.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val monoToken by viewModel.monoToken.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Підключення банків", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text("Monobank", style = MaterialTheme.typography.titleLarge)

                    if (isConnected) {
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isConnected) {
                    Button(
                        onClick = { viewModel.syncMonobank() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Синхронізувати зараз")
                        }
                    }

                    TextButton(
                        onClick = { viewModel.disconnectBank() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Відключити банк", color = Color.Red)
                    }
                } else {
                    if (!isInputVisible) {
                        Button(
                            onClick = { viewModel.toggleMonoInput(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Підключити")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openMonobankAuth() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отримати токен")
                        }
                        OutlinedTextField(
                            value = monoToken,
                            onValueChange = { viewModel.onTokenChange(it) },
                            label = { Text("Введіть токен") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = { viewModel.saveMonoToken() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Зберегти та підключити")
                        }
                    }
                }
            }
        }
    }
}