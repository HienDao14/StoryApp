package com.hiendao.presentation.voice.create.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiendao.coreui.R
import com.hiendao.presentation.voice.create.formatTime
import kotlinx.coroutines.delay

@Composable
fun ActiveRecordingScreen(
    recordingDuration: Int,
    onStopRecording: () -> Unit
) {
    val sampleText = stringResource(R.string.full_recording_script).trimIndent()

    val scrollState = rememberScrollState()
    
    // Auto-scroll logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(50) // Adjust speed here
            if (scrollState.canScrollForward) {
                scrollState.scrollBy(2f) // Adjust step here
            } else {
                 // Reset or stop? For now let's just stop or restart
                 // scrollState.scrollTo(0) 
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Timer and Status
        Text(
            text = "Đang ghi âm...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatTime(recordingDuration * 1000),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Scrolling Text Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = sampleText,
                style = MaterialTheme.typography.titleMedium.copy(
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.verticalScroll(scrollState)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stop Button
        IconButton(
            onClick = onStopRecording,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording",
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chạm để dừng",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
