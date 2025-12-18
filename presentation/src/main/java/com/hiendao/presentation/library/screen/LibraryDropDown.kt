package com.hiendao.presentation.library.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hiendao.coreui.R

@Composable
internal fun LibraryDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onImportEpub: () -> Unit,
    onGenerateStory: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.FileOpen, stringResource(id = R.string.import_epub))
            },
            text = { Text(stringResource(id = R.string.import_epub)) },
            onClick = {
                onImportEpub.invoke()
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.Create, stringResource(id = R.string.generate_story))
            },
            text = { Text(stringResource(id = R.string.generate_story)) },
            onClick = {
                onGenerateStory.invoke()
            }
        )
    }
}