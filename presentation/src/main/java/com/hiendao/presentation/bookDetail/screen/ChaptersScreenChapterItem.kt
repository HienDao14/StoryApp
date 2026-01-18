package com.hiendao.presentation.bookDetail.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hiendao.coreui.R
import com.hiendao.coreui.components.AnimatedTransition
import com.hiendao.coreui.theme.ColorNotice
import com.hiendao.coreui.theme.InternalTheme
import com.hiendao.coreui.theme.colorApp
import com.hiendao.data.local.entity.ChapterEntity
import com.hiendao.data.local.entity.ChapterWithContext
import kotlin.to

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun ChaptersScreenChapterItem(
    chapterWithContext: ChapterWithContext,
    selected: Boolean,
    isLocalSource: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    val chapter = chapterWithContext.chapter
    ListItem(
        headlineContent = {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            AnimatedTransition(
                targetState = chapterWithContext.lastReadChapter to chapter.read,
                transitionSpec = { fadeIn() togetherWith fadeOut(tween(delayMillis = 150)) }
            ) { (lastReadPosition, read) ->
                when {
                    lastReadPosition -> Text(
                        stringResource(id = R.string.last_read),
                        color = ColorNotice
                    )
                    read -> Text(stringResource(id = R.string.read))
                    else -> Text("")
                }
            }
        },
        colors = ListItemDefaults.colors(
            supportingColor = MaterialTheme.colorScheme.onTertiary,
            containerColor =
            if (selected) MaterialTheme.colorApp.tintedSelectedSurface
            else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .animateContentSize()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
    )
}


@Preview
@Composable
private fun PreviewView(
    @PreviewParameter(PreviewProvider::class) previewProviderState: PreviewProviderState
) {
    InternalTheme {
        ChaptersScreenChapterItem(
            chapterWithContext = previewProviderState.chapterWithContext,
            selected = previewProviderState.selected,
            isLocalSource = false,
            onLongClick = {},
            onClick = {},
            onDownload = {}
        )
    }
}


private data class PreviewProviderState(
    val chapterWithContext: ChapterWithContext,
    val selected: Boolean
)

private class PreviewProvider : PreviewParameterProvider<PreviewProviderState> {
    override val values = sequenceOf(
        PreviewProviderState(
            chapterWithContext = ChapterWithContext(
                chapter = ChapterEntity(
                    title = "Title of the chapter",
                    id = "url",
                    bookId = "bookUrl",
                    lastReadOffset = 0,
                    lastReadPosition = 0,
                    position = 0,
                    read = false
                ),
                downloaded = false,
                lastReadChapter = false
            ),
            selected = false
        ),
        PreviewProviderState(
            chapterWithContext = ChapterWithContext(
                chapter = ChapterEntity(
                    title = "Title of the chapter, Title of the chapter, Title of the chapter, Title of the chapter, Title of the chapter,Title of the chapter ,Title of the chapter",
                    id = "url",
                    bookId = "bookUrl",
                    lastReadOffset = 0,
                    lastReadPosition = 0,
                    position = 0,
                    read = true
                ),
                downloaded = true,
                lastReadChapter = false
            ),
            selected = false
        ),
        PreviewProviderState(
            chapterWithContext = ChapterWithContext(
                chapter = ChapterEntity(
                    title = "Title of the chapter",
                    id = "url",
                    bookId = "bookUrl",
                    lastReadOffset = 0,
                    lastReadPosition = 0,
                    position = 0,
                    read = false
                ),
                downloaded = true,
                lastReadChapter = true
            ),
            selected = true
        )
    )
}









