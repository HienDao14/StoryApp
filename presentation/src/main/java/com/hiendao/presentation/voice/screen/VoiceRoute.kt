package com.hiendao.presentation.voice.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hiendao.coreui.R
import com.hiendao.coreui.theme.ColorLike
import com.hiendao.coreui.theme.ColorNotice
import com.hiendao.coreui.utils.isAtTop
import com.hiendao.presentation.voice.section.VoiceReaderDropDown
import com.hiendao.presentation.voice.state.VoiceReaderScreenState

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceRoute(
    modifier: Modifier = Modifier,
    state: VoiceReaderScreenState,
    onFavouriteToggle: () -> Unit,
    onPressBack: () -> Unit,
    onChangeCover: () -> Unit,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onChapterSelected: (chapterUrl: String) -> Unit,
    onSelectModelVoice: (com.hiendao.coreui.appPreferences.VoicePredefineState) -> Unit
) {
    var showDropDown by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val textToSpeech = state.readerState?.settings?.textToSpeech

    val context = LocalContext.current
    val window = (context as? android.app.Activity)?.window

    // System Settings Interactions
    val brightness = state.readerState?.settings?.brightness?.value
    val keepScreenOn = state.readerState?.settings?.keepScreenOn?.value ?: false
    val nightMode = state.readerState?.settings?.nightMode?.value ?: false

    // Apply brightness and keep screen on
    DisposableEffect(brightness, keepScreenOn) {
        if (window != null) {
            val layoutParams = window.attributes
            val originalBrightness = layoutParams.screenBrightness
            
            if (brightness != null) {
                layoutParams.screenBrightness = brightness
            }
            window.attributes = layoutParams
            
            if (keepScreenOn) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            onDispose {
                // Revert brightness to preferred/system (or separate handling) if needed, 
                // but actually ReaderActivity doesn't revert strictly. 
                // However, for a Route in a shared Activity, we usually want to revert or let the next screen decide.
                // For now, we follow ReaderActivity's pattern but inside a Composable life-cycle.
                // Restoring original brightness is safer.
                layoutParams.screenBrightness = originalBrightness // This might be -1.0f (default)
                window.attributes = layoutParams
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } else {
            onDispose { }
        }
    }

    if (state.readerState?.showVoiceLoadingDialog?.value == true) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(
                        text = "(đang thực hiện, vui lòng chờ...)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            val isAtTop by lazyListState.isAtTop(threshold = 40.dp)
            val alpha by animateFloatAsState(targetValue = if (isAtTop) 0f else 1f, label = "")
            val backgroundColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.background.copy(alpha = alpha),
                label = ""
            )
            val titleColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha),
                label = ""
            )
            Surface(color = backgroundColor) {
                Column {
                    TopAppBar(
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                        title = {
                            Text(
                                text = state.book.value.title,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onPressBack
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = onFavouriteToggle
                            ) {
                                Icon(
                                    if (state.book.value.isFavourite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                    stringResource(R.string.open_the_web_view),
                                    tint = ColorLike
                                )
                            }
                        }
                    )
                    HorizontalDivider(Modifier.alpha(alpha))
                }
            }
            AnimatedVisibility(
                visible = state.isInSelectionMode.value,
                enter = expandVertically(initialHeight = { it / 2 }, expandFrom = Alignment.Top)
                        + fadeIn(),
                exit = shrinkVertically(targetHeight = { it / 2 }, shrinkTowards = Alignment.Top)
                        + fadeOut(),
            ) {

            }
        },
        content = { innerPadding ->
            VoiceScreenPart2(
                modifier = Modifier.fillMaxSize(),
                paddingValues = innerPadding,
                state = state,
                onChapterSelected = { chapterUrl ->
                    onChapterSelected.invoke(chapterUrl)
                },
                onPlayClick = { onPlayClick.invoke() },
                onPauseClick = { onPauseClick.invoke() },
                onSelectModelVoice = onSelectModelVoice
            )
        }
    )

    if (nightMode) {
        Box(
             modifier = Modifier
                 .fillMaxSize()
                 .background(Color(0x33FF9800)) // Amber overlay
        )
    }
}