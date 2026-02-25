package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.ui.components.FinCentraTopBar
import com.denisshulika.fincentra.ui.components.dream.DreamForm
import com.denisshulika.fincentra.ui.components.dream.DreamInfoCard
import com.denisshulika.fincentra.ui.components.dream.DreamProgressView
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinCentraTopBar(
                title = stringResource(R.string.dream_title_screen),
                isTopLevelScreen = false,
                onNavigationClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            DreamInfoCard()

            Spacer(modifier = Modifier.height(32.dp))

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
                            title = title,
                            target = target.toDoubleOrNull() ?: 0.0,
                            buffer = buffer.toDoubleOrNull() ?: 0.0,
                            currencyCode = progressState?.dream?.currencyCode ?: 980
                        )
                        isEditMode = false
                    },
                    onCancel = {
                        if (progressState != null) isEditMode = false
                    }
                )
            } else {
                progressState?.let { data ->
                    DreamProgressView(
                        progressData = data,
                        onEditClick = { isEditMode = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}