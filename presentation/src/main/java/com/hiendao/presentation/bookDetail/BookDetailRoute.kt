package com.hiendao.presentation.bookDetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Category

@Composable
fun BookDetailRoute(
    modifier: Modifier = Modifier,
    onFavClick: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onReadClick: (String) -> Unit = {},
    onPlayAudioClick: (String) -> Unit = {},
    onSummaryClick: (String) -> Unit = {}
) {


}