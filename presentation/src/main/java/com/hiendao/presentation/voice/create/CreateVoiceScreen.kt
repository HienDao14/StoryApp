package com.hiendao.presentation.voice.create

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.hiendao.coreui.components.MyOutlinedTextField
import com.hiendao.coreui.R as CoreR
import com.hiendao.presentation.voice.create.components.ActiveRecordingScreen
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoiceScreen(
    state: CreateVoiceUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: () -> Unit,
    onCreateVoice: () -> Unit,
    onDismissSuccessDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreR.string.create_new_voice_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreR.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasPermission = isGranted
            }
        )

        LaunchedEffect(key1 = true) {
            if (!hasPermission) {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(stringResource(CoreR.string.voice_name_label), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            MyOutlinedTextField(
                value = state.voiceName,
                onValueChange = onNameChange,
                placeHolderText = stringResource(CoreR.string.voice_name_placeholder),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (state.isRecording) {
                ActiveRecordingScreen(
                    recordingDuration = state.recordingDuration,
                    onStopRecording = onStopRecording
                )
            } else {
                RecordingSection(
                    state = state,
                    onStartRecording = {
                        if (hasPermission) {
                            onStartRecording()
                        } else {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopRecording = onStopRecording,
                    onPlayRecording = onPlayRecording
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(stringResource(CoreR.string.cancel), color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = onCreateVoice,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    enabled = state.voiceName.isNotBlank() && state.recordedFile != null && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.surface)
                    } else {
                        Text(stringResource(CoreR.string.action_save_voice))
                    }
                }
            }
        }

        if (state.showSuccessDialog) {
            val title = if (state.successMessage != null) stringResource(CoreR.string.notification) else stringResource(CoreR.string.notification)
            val message = state.successMessage ?: "Đang trong quá trình tạo giọng. Sẽ mất 1 khoảng thời gian nên người dùng có thể sử dụng tính năng khác, tôi sẽ thông báo sau khi thành công."

            AlertDialog(
                onDismissRequest = onDismissSuccessDialog,
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = onDismissSuccessDialog) {
                        Text(stringResource(CoreR.string.str_ok))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun RecordingSection(
    state: CreateVoiceUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(24.dp)
    ) {
        if (!state.isRecording && state.recordedFile == null) {
            // Initial State
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .border(1.dp, Color.LightGray, CircleShape)
                    .clickable(onClick = onStartRecording),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(CoreR.string.content_desc_start_recording),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(CoreR.string.press_to_record), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else if (state.isRecording) {
            // Recording State
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onStopRecording),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = stringResource(CoreR.string.content_desc_stop_recording),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(CoreR.string.status_recording), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = String.format("%02d : %02d", state.recordingDuration / 60, state.recordingDuration % 60),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Fake Visualizer
            Row(
                modifier = Modifier.height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(20) {
                    val height = remember { Random.nextInt(10, 40) }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    )
                }
            }
        } else {
            // Recorded State
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onPlayRecording) {
                    if (state.isPlaying) {
                        Icon(Icons.Default.Stop, contentDescription = stringResource(CoreR.string.content_desc_stop))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(CoreR.string.action_stop_listening), color = MaterialTheme.colorScheme.onBackground)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(CoreR.string.content_desc_play))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(CoreR.string.action_preview_voice), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(CoreR.string.status_recorded, state.recordedFile?.name ?: ""), 
                style = MaterialTheme.typography.bodySmall, 
                color = Color.Gray
            )

            // Playback Progress UI
            if (state.recordedFile != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.playbackDuration > 0) {
                        LinearProgressIndicator(
                            progress = { state.playbackPosition.toFloat() / state.playbackDuration.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        )
                    } else {
                         LinearProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(state.playbackPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = formatTime(state.playbackDuration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
             
             Spacer(modifier = Modifier.height(8.dp))
             Text(
                text = stringResource(CoreR.string.hint_record_again),
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onBackground,
                 modifier = Modifier.clickable(onClick = onStartRecording)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Instructions
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(stringResource(CoreR.string.label_instructions), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        val instructions = listOf(
            stringResource(CoreR.string.instruction_1),
            stringResource(CoreR.string.instruction_2),
            stringResource(CoreR.string.instruction_3)
        )
        instructions.forEach {
            Text("•  $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}