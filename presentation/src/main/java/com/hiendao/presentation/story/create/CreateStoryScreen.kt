package com.hiendao.presentation.story.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hiendao.coreui.components.MyButton
import com.hiendao.coreui.components.MyOutlinedTextField

@Composable
fun CreateStoryRoute(
    viewModel: CreateStoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CreateStoryScreen(
        state = state,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onFreeTextChange = viewModel::onFreeTextChange,
        onDurationChange = viewModel::onDurationChange,
        onLanguageChange = viewModel::onLanguageChange,
        onGenerate = viewModel::generateStory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    state: CreateStoryUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onFreeTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onLanguageChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Story form AI") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            MyOutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                placeHolderText = "Title"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Free Text
            MyOutlinedTextField(
                value = state.freeText,
                onValueChange = onFreeTextChange,
                placeHolderText = "Main Context (Free Text)"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Duration
            MyOutlinedTextField(
                value = state.durationSeconds.toString(),
                onValueChange = { onDurationChange(it.toIntOrNull() ?: 300) },
                placeHolderText = "Duration (seconds)"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Language
            var expanded by remember { mutableStateOf(false) }
            val languages = listOf("Vietnam" to "vi", "English" to "en", "Chinese" to "zh") // Display to Value
            val selectedLanguageDisplay = languages.find { it.second == state.language }?.first ?: "Vietnam"

            androidx.compose.material3.ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                MyOutlinedTextField(
                    value = selectedLanguageDisplay,
                    onValueChange = {},
                    placeHolderText = "Language",
                    readOnly = true,
                    trailingIcon = {
                        androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    languages.forEach { (display, value) ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = display) },
                            onClick = {
                                onLanguageChange(value)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Generate Button
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                MyButton(
                    text = "Generate Story",
                    modifier = Modifier.align(Alignment.CenterHorizontally), 
                    onClick = onGenerate,
                    backgroundColor = MaterialTheme.colorScheme.primary
                )
            }
            
            if (state.successMessage != null) {
                Text(text = state.successMessage!!, color = MaterialTheme.colorScheme.primary)
            }
            if (state.errorMessage != null) {
                Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
