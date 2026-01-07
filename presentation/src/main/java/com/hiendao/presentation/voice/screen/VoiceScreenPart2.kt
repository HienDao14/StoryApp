package com.hiendao.presentation.voice.screen

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Popup
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.components.ImageView
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.voice.screen.components.SearchableSelectionDialog
import com.hiendao.presentation.voice.screen.components.progressBorder
import com.hiendao.presentation.voice.state.VoiceReaderScreenState

import com.hiendao.presentation.utils.parseHtml

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceScreenPart2(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    state: VoiceReaderScreenState,
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onChangeVolume: (value: Float) -> Unit = {},
    onChangeVoiceSpeed: (value: Float) -> Unit = {},
    onChangeVoice: (value: String) -> Unit = {},
    onChangeTimeStamp: (value: Long) -> Unit = {},
    onCoverLongClick: () -> Unit = {},
    onChapterSelected: (chapterUrl: String) -> Unit = {},
    onSelectModelVoice: (com.hiendao.coreui.appPreferences.VoicePredefineState) -> Unit = {}
) {
    val book = state.book.value
    val coverImageModel = book.coverImageUrl?.let {
        rememberResolvedBookImagePath(
            bookUrl = book.url,
            imagePath = it
        )
    }

    val textToSpeech = state.readerState?.settings?.textToSpeech
    val audioProgress = state.audioProgress.value
    val isPlaying = textToSpeech?.isPlaying?.value ?: false
    val activeVoice = textToSpeech?.activeVoice?.value
    val activeAiVoice = textToSpeech?.activeAiVoice?.value
    val availableVoices = textToSpeech?.availableVoices ?: emptyList()
    val currentTextPlaying = state.currentTextPlaying?.value
    val chapters = state.chapters
    
    // Style Settings
    val style = state.readerState?.settings?.style
    val textSize = style?.textSize?.value ?: 20f
    val textFont = style?.textFont?.value ?: "Arial"
    val lineHeight = style?.lineHeight?.value ?: 1.5f
    val textAlign = style?.textAlign?.value ?: 0 // 0: Left, 1: Justify
    val screenMargin = style?.screenMargin?.value ?: 16
    
    // Dialog states
    var showChapterDialog by rememberSaveable { mutableStateOf(false) }
    var showVoiceDialog by rememberSaveable { mutableStateOf(false) }

    // System Volume
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var currentVolume by remember { mutableFloatStateOf(0.5f) }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    
    LaunchedEffect(Unit) {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        currentVolume = current / maxVolume
    }
    
    fun setVolume(newVolumeNormalized: Float) {
         currentVolume = newVolumeNormalized
         val newVol = (newVolumeNormalized * maxVolume).toInt()
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
    }

    // Popup states
    var showVolumePopup by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Volume Control
                    Box {
                        IconButton(onClick = { showVolumePopup = true }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, "Volume")
                        }
                        if (showVolumePopup) {
                            Popup(
                                alignment = Alignment.BottomStart,
                                onDismissRequest = { showVolumePopup = false },
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.padding(bottom = 48.dp).width(200.dp) // Offset above the button
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Volume", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = currentVolume,
                                            onValueChange = { setVolume(it) }
                                        )
                                        DataControlSlider(
                                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                                            label = "Volume",
                                            value = currentVolume,
                                            onValueChange = { setVolume(it) },
                                            valueRange = 0f..10f,
                                            displayValue = String.format("%.1fx", currentVolume)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Playback Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                         IconButton(onClick = { textToSpeech?.playPreviousChapter?.invoke() }) {
                            Icon(Icons.Filled.SkipPrevious, "Prev Chapter", modifier = Modifier.size(28.dp))
                         }
                        IconButton(onClick = { textToSpeech?.playPreviousItem?.invoke() }) {
                            Icon(Icons.Filled.FastRewind, "Rewind", modifier = Modifier.size(24.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { if (isPlaying) onPauseClick() else onPlayClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = { textToSpeech?.playNextItem?.invoke() }) {
                            Icon(Icons.Filled.FastForward, "Forward", modifier = Modifier.size(24.dp))
                        }
                         IconButton(onClick = { textToSpeech?.playNextChapter?.invoke() }) {
                            Icon(Icons.Filled.SkipNext, "Next Chapter", modifier = Modifier.size(28.dp))
                         }
                    }

                    // Settings Control
                    Box {
                        IconButton(onClick = { showSettingsPopup = true }) {
                            Icon(Icons.Default.Tune, "Settings")
                        }
                        if (showSettingsPopup) {
                            Popup(
                                alignment = Alignment.BottomEnd,
                                onDismissRequest = { showSettingsPopup = false }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.padding(bottom = 48.dp).width(250.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        DataControlSlider(
                                            icon = Icons.Default.Speed,
                                            label = "Speed",
                                            value = textToSpeech?.voiceSpeed?.value ?: 1f,
                                            onValueChange = { textToSpeech?.setVoiceSpeed?.invoke(it) },
                                            valueRange = 0.5f..3.0f,
                                            displayValue = String.format("%.1fx", textToSpeech?.voiceSpeed?.value ?: 1f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DataControlSlider(
                                            icon = Icons.Default.GraphicEq,
                                            label = "Pitch",
                                            value = textToSpeech?.voicePitch?.value ?: 1f,
                                            onValueChange = { textToSpeech?.setVoicePitch?.invoke(it) },
                                            valueRange = 0.5f..2.0f,
                                            displayValue = String.format("%.1f", textToSpeech?.voicePitch?.value ?: 1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Cover Art (Reduced Size)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                 if (coverImageModel != null) {
                    ImageView(
                        imageModel = coverImageModel,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                     Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Default Cover",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Title & Chapter
            Text(
                text = book.title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
             Text(
                text = audioProgress?.chapterTitle ?: "Select a Chapter", 
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                 textAlign = TextAlign.Center,
                 maxLines = 1,
                 overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Selection Dialog Triggers (Compact)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chapter Selector
                OutlinedButton(
                    onClick = { showChapterDialog = true },
                    modifier = Modifier.weight(1f),
                     contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Chapters", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
                }
                
                // Voice Selector
                OutlinedButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val displayVoice = remember(activeAiVoice, activeVoice) {
                        activeAiVoice?.savedName ?: activeVoice?.language ?: "Voice"
                    }
                    Text(text = displayVoice, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
                }
            }
            
            // Dialogs (Keep as is)
             if (showChapterDialog) {
                SearchableSelectionDialog(
                    title = "Select Chapter",
                    items = chapters,
                    selectedItem = chapters.find { it.chapter.id == book.lastReadChapter },
                    onItemSelected = { 
                        onChapterSelected(it.chapter.id)
                        showChapterDialog = false
                    },
                    onDismissRequest = { showChapterDialog = false },
                    itemToString = { it.chapter.title },
                    leadingIcon = { 
                        if (it.chapter.id == book.lastReadChapter) 
                            Icon(Icons.Filled.Equalizer, null, tint = MaterialTheme.colorScheme.primary) 
                    }
                )
            }
            
            if (showVoiceDialog) {
                // Prepare unified list
                val aiVoices = textToSpeech?.customSavedVoices?.value ?: emptyList()
                val systemVoices = availableVoices ?: emptyList()
                
                // We use a simple wrapper to display both
                data class VoiceOption(
                    val id: String, 
                    val name: String, 
                    val isAi: Boolean,
                    val originalAi: com.hiendao.coreui.appPreferences.VoicePredefineState? = null,
                    val originalSystem: com.hiendao.domain.text_to_speech.VoiceData? = null
                )
                
                val voiceOptions = derivedStateOf {
                     val list = mutableListOf<VoiceOption>()
                     list.addAll(aiVoices.map { VoiceOption(it.voiceId, "AI: ${it.savedName}", true, originalAi = it) })
                     list.addAll(systemVoices.map { VoiceOption(it.id, "System: ${it.language}", false, originalSystem = it) })
                     list
                }

                SearchableSelectionDialog(
                    title = "Select Voice",
                    items = voiceOptions.value,
                    selectedItem = voiceOptions.value.find { 
                        if (activeAiVoice != null) it.isAi && it.originalAi?.modelId == activeAiVoice.modelId
                        else !it.isAi && it.id == activeVoice?.id 
                    },
                    onItemSelected = { option ->
                        if (option.isAi) {
                            option.originalAi?.let { onSelectModelVoice(it) }
                        } else {
                            option.originalSystem?.let { 
                                textToSpeech?.setVoiceId?.invoke(it.id) 
                            }
                        }
                        showVoiceDialog = false
                    },
                    onDismissRequest = { showVoiceDialog = false },
                    itemToString = { it.name },
                    trailingIcon = { option ->
                         val isSelected = if (activeAiVoice != null) option.isAi && option.originalAi?.modelId == activeAiVoice.modelId
                                          else !option.isAi && option.id == activeVoice?.id
                        if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Current Text (Flexible Weight)
            val progress = (audioProgress?.progressPercentage ?: 0f) / 100f
            
            Box(
                modifier = Modifier
                    .weight(1f) // Take remaining space
                    .fillMaxWidth()
                    .progressBorder(
                        progress = progress, 
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(0.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                 val textToDisplay = currentTextPlaying?.itemPos?.let { itemPos ->
                     when (itemPos) {
                         is ReaderItem.Text -> itemPos.textToDisplay
                         else -> "..."
                     }
                 } ?: "..."
                 
                val scrollState = rememberScrollState()
                
                LaunchedEffect(textToDisplay) {
                    scrollState.scrollTo(0)
                }

                Text(
                    text = textToDisplay.parseHtml(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = androidx.compose.ui.unit.TextUnit(textSize, androidx.compose.ui.unit.TextUnitType.Sp),
                        lineHeight = androidx.compose.ui.unit.TextUnit(textSize * lineHeight, androidx.compose.ui.unit.TextUnitType.Sp),
                        textAlign = if (textAlign == 0) TextAlign.Start else TextAlign.Justify
                    ),
                    textAlign = if (textAlign == 0) TextAlign.Start else TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = screenMargin.dp)
                        .verticalScroll(scrollState)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
