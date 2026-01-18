package com.hiendao.presentation.voice.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun <T> SearchableSelectionDialog(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    itemToString: (T) -> String,
    leadingIcon: @Composable ((T) -> Unit)? = null,
    trailingIcon: @Composable ((T) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemToString(it).contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(com.hiendao.coreui.R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val isSelected = item == selectedItem
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = itemToString(item),
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingContent = leadingIcon?.let { { it(item) } },
                            trailingContent = trailingIcon?.let { { it(item) } },
                            modifier = Modifier
                                .clickable { onItemSelected(item) }
                                .fillMaxWidth(),
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f) else androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}
