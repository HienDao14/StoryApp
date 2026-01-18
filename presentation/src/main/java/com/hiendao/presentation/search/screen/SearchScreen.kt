package com.hiendao.presentation.search.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.R
import com.hiendao.coreui.components.BookImageButtonView
import com.hiendao.coreui.modifiers.bounceOnPressed
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.ImageBorderShape
import com.hiendao.coreui.utils.isLocalUri
import com.hiendao.domain.model.Book
import com.hiendao.domain.utils.rememberResolvedBookImagePath

@Composable
fun SearchScreen(
    list: List<Book>,
    onClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
    isLoading: Boolean = false,
    isEndReached: Boolean = false
) {


    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 400.dp, start = 4.dp, end = 4.dp)
    ) {
        items(
            items = list,
            key = { it.id }
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(modifier = Modifier.clickable{
                onClick(it)
            }) {
                BookImageButtonView(
                    title = it.title,
                    coverImageModel = rememberResolvedBookImagePath(
                        bookUrl = it.id,
                        imagePath = it.coverImageUrl
                    ),
                    onClick = { onClick(it) },
                    interactionSource = interactionSource,
                    modifier = Modifier.bounceOnPressed(interactionSource)
                )

                if (it.id.isLocalUri) Text(
                    text = stringResource(R.string.local),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(ColorAccent, ImageBorderShape)
                        .padding(4.dp)
                )
            }
            if (list.indexOf(it) >= list.size - 1 && !isLoading && !isEndReached) {
                onLoadMore.invoke()
            }
        }
    }
}
