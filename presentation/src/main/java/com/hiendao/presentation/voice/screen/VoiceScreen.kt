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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.components.ImageView
import com.hiendao.domain.text_to_speech.VoiceData
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.voice.state.VoiceReaderScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceScreen(
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
    val audioProgress = state.audioProgress
    val isPlaying = textToSpeech?.isPlaying?.value ?: false
    val activeVoice = textToSpeech?.activeVoice?.value
    val availableVoices = textToSpeech?.availableVoices ?: emptyList()
    val currentTextPlaying = state.currentTextPlaying
    val chapters = state.chapters

    // Dropdown states
    var showChapterDropdown by rememberSaveable { mutableStateOf(false) }
    var showVoiceDropdown by rememberSaveable { mutableStateOf(false) }
    
    // System Volume
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var currentVolume by remember { mutableFloatStateOf(0.5f) }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    
    // Sync initial volume
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                     Text(
                        text = "Voice Reader", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    ) 
                },
                actions = {
                    // Placeholder for future actions if needed
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
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
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Cover Art
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
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
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title & Chapter Info
            Text(
                text = book.title, 
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = audioProgress?.chapterTitle ?: "Select a Chapter", 
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                 textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Selection Row: Chapter & Voice Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chapter Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showChapterDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                         Text(
                             text = stringResource(com.hiendao.coreui.R.string.chapters),
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis,
                             modifier = Modifier.weight(1f)
                         )
                         Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = showChapterDropdown,
                        onDismissRequest = { showChapterDropdown = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        chapters.forEach { chapter ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        chapter.chapter.title, 
                                        fontWeight = if (chapter.chapter.id == book.lastReadChapter) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    onChapterSelected(chapter.chapter.id)
                                    showChapterDropdown = false
                                },
                                leadingIcon = {
                                    if (chapter.chapter.id == book.lastReadChapter) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }
                    }
                }

                // Voice Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showVoiceDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = activeVoice?.language ?: "Voice",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        availableVoices.forEach { voice ->
                             DropdownMenuItem(
                                text = { Text(voice.language) },
                                onClick = {
                                    textToSpeech?.setVoiceId?.invoke(voice.id)
                                    showVoiceDropdown = false
                                },
                                trailingIcon = {
                                    if (voice.id == activeVoice?.id) {
                                        Icon(Icons.Default.Check, null)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // 4. Current Text Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
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
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 5. Audio Settings Sliders
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Volume
                DataControlSlider(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Volume",
                    value = currentVolume,
                    onValueChange = { setVolume(it) }
                )
                // Speed
                DataControlSlider(
                    icon = Icons.Default.Speed,
                    label = "Speed",
                    value = textToSpeech?.voiceSpeed?.value ?: 1f,
                    onValueChange = { textToSpeech?.setVoiceSpeed?.invoke(it) },
                    valueRange = 0.5f..3.0f,
                    displayValue = String.format("%.1fx", textToSpeech?.voiceSpeed?.value ?: 1f)
                )
                 // Pitch
                DataControlSlider(
                    icon = Icons.Default.GraphicEq,
                    label = "Pitch",
                    value = textToSpeech?.voicePitch?.value ?: 1f,
                    onValueChange = { textToSpeech?.setVoicePitch?.invoke(it) },
                    valueRange = 0.5f..2.0f,
                    displayValue = String.format("%.1f", textToSpeech?.voicePitch?.value ?: 1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // 6. Playback Controls Row (5 buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Prev Chapter
                 IconButton(onClick = { textToSpeech?.playPreviousChapter?.invoke() }) {
                    Icon(Icons.Filled.SkipPrevious, "Prev Chapter", modifier = Modifier.size(32.dp))
                 }
                 
                 // Prev Item
                 IconButton(onClick = { textToSpeech?.playPreviousItem?.invoke() }) {
                    Icon(Icons.Filled.FastRewind, "Rewind", modifier = Modifier.size(32.dp)) 
                 }
                 
                 // Play/Pause
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
                 
                 // Next Item
                 IconButton(onClick = { textToSpeech?.playNextItem?.invoke() }) {
                    Icon(Icons.Filled.FastForward, "Forward", modifier = Modifier.size(32.dp))
                 }
                 
                 // Next Chapter
                 IconButton(onClick = { textToSpeech?.playNextChapter?.invoke() }) {
                    Icon(Icons.Filled.SkipNext, "Next Chapter", modifier = Modifier.size(32.dp))
                 }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DataControlSlider(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    displayValue: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon, 
            contentDescription = label, 
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val percentage = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start) * 100).toInt()
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(displayValue ?: "$percentage%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.height(20.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}