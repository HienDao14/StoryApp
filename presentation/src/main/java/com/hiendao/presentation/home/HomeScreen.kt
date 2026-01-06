package com.hiendao.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hiendao.coreui.R
import com.hiendao.coreui.components.BookImageButtonView
import com.hiendao.coreui.modifiers.bounceOnPressed
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.ImageBorderShape
import com.hiendao.coreui.utils.isLocalUri
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Category
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import com.hiendao.presentation.home.viewModel.HomeState

@Composable
fun FavouriteSection(
    modifier: Modifier = Modifier,
    title: String,
    listBooks: List<Book>,
    onBookClick: (Book) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    uiState: HomeState,
    isEndReached: Boolean = false
) {
    val context = LocalContext.current

    Column(modifier
        .fillMaxWidth()
        .wrapContentHeight()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = context.getString(R.string.see_all),
                style = MaterialTheme.typography.bodyMedium,
                color = ColorAccent,
                modifier = Modifier
                    .clickable { onSeeAllClick() }
                    .padding(4.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ){
            items(listBooks.size) {
                val book = listBooks[it]
                val interactionSource = remember { MutableInteractionSource() }
                Box {
                    BookImageButtonView(
                        title = book.title,
                        coverImageModel = rememberResolvedBookImagePath(
                            bookUrl = book.id,
                            imagePath = book.coverImageUrl
                        ),
                        onClick = { onBookClick.invoke(book) },
                        interactionSource = interactionSource,
                        modifier = Modifier.bounceOnPressed(interactionSource)
                    )
                }
                if (it >= listBooks.size - 1 && !uiState.isLoading && !isEndReached) {
                    onLoadMore.invoke()
                }
            }
        }
    }
}


// ====== 2) Categories (hình tròn + tên) ======
@Composable
fun CategorySection(
    title: String,
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier
        .fillMaxWidth()
        .wrapContentHeight()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }

        // Cuộn ngang
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ){
            items(categories) { cat ->
                Column(
                    modifier = Modifier
                        .width(90.dp)
                        .clickable {
                            onCategoryClick(cat)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = cat.iconUrl,
                            contentDescription = cat.name,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

// ====== 3) Grid 2 cột trong cùng cuộn (FlowRow) ======
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookGridFlow(
    title: String,
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onPlayClick: (Book) -> Unit,
    onFavouriteClick: (Book) -> Unit,
    onLoadMore: () -> Unit = {},
    isLoading: Boolean = false,
    isEndReached: Boolean = false
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        // FlowRow để không tạo nested scroll
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 1000.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            columns = GridCells.Fixed(2)
        ) {
            items(books.size) { index ->
                val book = books[index]
                HomeItem(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    book = book,
                    onFavoriteClick = {onFavouriteClick(book)},
                    onPlayClick = {onPlayClick(book)},
                    onItemClick = { onBookClick(book) }
                )

                if (index >= books.size - 1 && !isLoading && !isEndReached) {
                    onLoadMore.invoke()
                }
            }
        }
    }
}

@Composable
fun BookGridCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // ảnh chiếm trên
            )
            Column(Modifier.padding(10.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}