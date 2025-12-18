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
    onChapterSelected: (chapterUrl: String) -> Unit = {}
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
    val availableVoices = textToSpeech?.availableVoices ?: emptyList()
    val currentTextPlaying = state.currentTextPlaying?.value
    val chapters = state.chapters
    
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

    Scaffold(
        // Removed TopAppBar as requested
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Extra top spacing since TopBar is gone

            // 1. Cover Art
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
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title & Chapter
            Text(
                text = book.title, 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = audioProgress?.chapterTitle ?: "Select a Chapter", 
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                 textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // 3. Selection Dialog Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chapter Selector
                OutlinedButton(
                    onClick = { showChapterDialog = true },
                    modifier = Modifier.weight(1f),
                     contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Chapters", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                // Voice Selector
                OutlinedButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = activeVoice?.language ?: "Voice", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            // Dialogs
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
                SearchableSelectionDialog(
                    title = "Select Voice",
                    items = availableVoices.toList(),
                    selectedItem = activeVoice,
                    onItemSelected = { 
                        textToSpeech?.setVoiceId?.invoke(it.id)
                        showVoiceDialog = false
                    },
                    onDismissRequest = { showVoiceDialog = false },
                    itemToString = { it.language },
                    trailingIcon = {
                        if (it.id == activeVoice?.id) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Current Text with Progress Border
            // Determine progress: 0-1.
            // audioProgress?.progressPercentage is 0-100?
            // Actually progress border should probably track ITEM progress?
            // User said "progress from 0-100%" - presumably Chapter progress.
            val progress = (audioProgress?.progressPercentage ?: 0f) / 100f
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .progressBorder(
                        progress = progress, 
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(0.dp)) // Border is rectangular, keeping bg rectangular.
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                 val textToDisplay = currentTextPlaying?.itemPos?.let { itemPos ->
                     when (itemPos) {
                         is ReaderItem.Text -> itemPos.textToDisplay
                         else -> "..."
                     }
                 } ?: "..."
                 
                Text(
                    text = textToDisplay,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 5. Sliders and Controls (Same as before)
            // Volume
            DataControlSlider(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "Volume",
                value = currentVolume,
                onValueChange = { setVolume(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Speed
            DataControlSlider(
                icon = Icons.Default.Speed,
                label = "Speed",
                value = textToSpeech?.voiceSpeed?.value ?: 1f,
                onValueChange = { textToSpeech?.setVoiceSpeed?.invoke(it) },
                valueRange = 0.5f..3.0f,
                displayValue = String.format("%.1fx", textToSpeech?.voiceSpeed?.value ?: 1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
             // Pitch
            DataControlSlider(
                icon = Icons.Default.GraphicEq,
                label = "Pitch",
                value = textToSpeech?.voicePitch?.value ?: 1f,
                onValueChange = { textToSpeech?.setVoicePitch?.invoke(it) },
                valueRange = 0.5f..2.0f,
                displayValue = String.format("%.1f", textToSpeech?.voicePitch?.value ?: 1f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // 6. Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 IconButton(onClick = { textToSpeech?.playPreviousChapter?.invoke() }) {
                    Icon(Icons.Filled.SkipPrevious, "Prev Chapter", modifier = Modifier.size(32.dp))
                 }
                 IconButton(onClick = { textToSpeech?.playPreviousItem?.invoke() }) {
                    Icon(Icons.Filled.FastRewind, "Rewind", modifier = Modifier.size(32.dp)) 
                 }
                 Box(
                     modifier = Modifier
                         .size(72.dp)
                         .clip(CircleShape)
                         .background(MaterialTheme.colorScheme.primary)
                         .clickable { if (isPlaying) onPauseClick() else onPlayClick() },
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                         imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                         contentDescription = "Play/Pause",
                         tint = MaterialTheme.colorScheme.onPrimary,
                         modifier = Modifier.size(40.dp)
                     )
                 }
                 IconButton(onClick = { textToSpeech?.playNextItem?.invoke() }) {
                    Icon(Icons.Filled.FastForward, "Forward", modifier = Modifier.size(32.dp))
                 }
                 IconButton(onClick = { textToSpeech?.playNextChapter?.invoke() }) {
                    Icon(Icons.Filled.SkipNext, "Next Chapter", modifier = Modifier.size(32.dp))
                 }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
