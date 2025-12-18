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
        onReadingLevelChange = viewModel::onReadingLevelChange,
        onGenreChange = viewModel::onGenreChange,
        onToneChange = viewModel::onToneChange,
        onKeyMessageAdd = viewModel::onKeyMessageAdd,
        onKeyMessageRemove = viewModel::onKeyMessageRemove,
        onCharacterAdd = viewModel::onCharacterAdd,
        onCharacterRemove = viewModel::onCharacterRemove,
        onIncludeSoundCuesChange = viewModel::onIncludeSoundCuesChange,
        onLanguageChange = viewModel::onLanguageChange,
        onAdditionalInstructionsChange = viewModel::onAdditionalInstructionsChange,
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
    onReadingLevelChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onKeyMessageAdd: (String) -> Unit,
    onKeyMessageRemove: (Int) -> Unit,
    onCharacterAdd: (String, String) -> Unit,
    onCharacterRemove: (Int) -> Unit,
    onIncludeSoundCuesChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onAdditionalInstructionsChange: (String) -> Unit,
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

            // Reading Level
            // Simple validation or text input for now as per minimal friction
            MyOutlinedTextField(
                value = state.readingLevel,
                onValueChange = onReadingLevelChange,
                placeHolderText = "Reading Level (e.g. 5-10)"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Genre
            MyOutlinedTextField(
                value = state.genre,
                onValueChange = onGenreChange,
                placeHolderText = "Genre"
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tone
             MyOutlinedTextField(
                value = state.tone,
                onValueChange = onToneChange,
                placeHolderText = "Tone"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Language
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Language: ", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                // Simple Toggle for demonstration since only 2 langs
                MyButton(
                    text = "VI",
                    selected = state.language == "vi",
                    onClick = { onLanguageChange("vi") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MyButton(
                    text = "EN",
                    selected = state.language == "en",
                    onClick = { onLanguageChange("en") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Sound Cues
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Include Sound Cues")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = state.includeSoundCues,
                    onCheckedChange = onIncludeSoundCuesChange
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Characters Section
            Text(text = "Characters", style = MaterialTheme.typography.titleMedium)
            state.characters.forEachIndexed { index, char ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Name: ${char.name}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Desc: ${char.shortDescription}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onCharacterRemove(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
            // Add Character Input (Simple local state)
            var newCharName by remember { mutableStateOf("") }
            var newCharDesc by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                     MyOutlinedTextField(
                        value = newCharName,
                        onValueChange = { newCharName = it },
                        placeHolderText = "Char Name"
                    )
                     MyOutlinedTextField(
                        value = newCharDesc,
                        onValueChange = { newCharDesc = it },
                        placeHolderText = "Char Desc"
                    )
                }
                IconButton(onClick = {
                    onCharacterAdd(newCharName, newCharDesc)
                    newCharName = ""
                    newCharDesc = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Key Messages
            Text(text = "Key Messages", style = MaterialTheme.typography.titleMedium)
             state.keyMessages.forEachIndexed { index, msg ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "- $msg", modifier = Modifier.weight(1f))
                    IconButton(onClick = { onKeyMessageRemove(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
             var newMsg by remember { mutableStateOf("") }
             Row(verticalAlignment = Alignment.CenterVertically) {
                 MyOutlinedTextField(
                    value = newMsg,
                    onValueChange = { newMsg = it },
                    placeHolderText = "New Message",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    onKeyMessageAdd(newMsg)
                    newMsg = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
             }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Instructions
            MyOutlinedTextField(
                value = state.additionalInstructions,
                onValueChange = onAdditionalInstructionsChange,
                placeHolderText = "Additional Instructions"
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Generate Button
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                MyButton(
                    text = "Generate Story",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGenerate
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
