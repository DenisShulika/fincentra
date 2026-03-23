package com.denisshulika.fincentra.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val title by viewModel.title.collectAsStateWithLifecycle()
    val target by viewModel.target.collectAsStateWithLifecycle()
    val buffer by viewModel.buffer.collectAsStateWithLifecycle()
    val emoji by viewModel.emoji.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(progressState) {
        if (progressState != null && !isEditMode) {
            viewModel.prepareForEdit(progressState!!.dream)
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
                    onTitleChange = viewModel::onTitleChange,
                    target = target,
                    onTargetChange = viewModel::onTargetChange,
                    buffer = buffer,
                    onBufferChange = viewModel::onBufferChange,
                    emoji = emoji,
                    onEmojiChange = viewModel::onEmojiChange,
                    isLoading = isLoading,
                    isExistingDream = progressState != null,
                    onSave = {
                        viewModel.updateDream(progressState?.dream?.currencyCode ?: 980)
                        isEditMode = false
                    },
                    onCancel = {
                        if (progressState != null) {
                            viewModel.prepareForEdit(progressState!!.dream)
                            isEditMode = false
                        }
                    },
                    onDelete = { showDeleteConfirm = true }
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Dream?") },
            text = { Text("This will reset your goal progress. Your transactions will remain safe.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDream()
                    showDeleteConfirm = false
                    isEditMode = false
                }) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}