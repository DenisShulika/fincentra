package com.denisshulika.fincentra.ui.components.transactions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel

@Composable
fun TransactionsTopBar(
    viewModel: TransactionsViewModel,
    onFilterCategoryClick: () -> Unit,
    onFilterTypeClick: () -> Unit,
    onFilterDateClick: () -> Unit
) {
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isSearchActive) {
            Text(
                text = "Транзакції",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(
                onClick = { viewModel.toggleSearch(true) }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Пошук")
            }
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFilterCategoryClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "Категорії")
                }
                IconButton(onClick = onFilterTypeClick) {
                    Icon(Icons.Default.Tune, contentDescription = "Банк/Тип")
                }
                IconButton(onClick = onFilterDateClick) {
                    Icon(Icons.Default.DateRange, contentDescription = "Дати")
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Пошук...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            IconButton(
                onClick = { viewModel.toggleSearch(false) }
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрити")
            }
        }
    }
}