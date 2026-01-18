package com.hiendao.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    state: GlobalPlayerState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit
) {
    if (!state.isVisible) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if(!state.coverUrl.isNullOrEmpty()){
                AsyncImage(
                    model = state.coverUrl,
                    contentDescription = state.bookTitle,
                    modifier = Modifier.size(52.dp)
                )
            }
            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.bookTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = if(state.chapterTitle.isNotEmpty()) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if(state.chapterTitle.isNotEmpty()) {
                    Text(
                        text = state.chapterTitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, enabled = !state.isLoading) {
                    Icon(Icons.Default.SkipPrevious, "Previous")
                }
                
                IconButton(onClick = onPlayPause, enabled = !state.isLoading) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play/Pause"
                    )
                }

                 IconButton(onClick = onNext, enabled = !state.isLoading) {
                    Icon(Icons.Default.SkipNext, "Next")
                }
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
        }
        
        // Progress Indicator at bottom?
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                )
            } else if (state.progress > 0) {
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
