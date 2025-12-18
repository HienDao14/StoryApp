package com.hiendao.presentation.home.autoSwipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.hiendao.coreui.R
import com.hiendao.coreui.components.BookImageButtonView
import com.hiendao.coreui.components.ImageView
import com.hiendao.coreui.modifiers.bounceOnPressed
import com.hiendao.coreui.theme.ImageBorderShape
import com.hiendao.coreui.theme.colorApp
import com.hiendao.domain.model.Book
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutoSwipePager(
    modifier: Modifier = Modifier,
    books: List<Book>,
    navigate: (Book) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        books.size
    }

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(books.size)
    )

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxWidth(),
            state = pagerState,
            flingBehavior = fling,
            key = {
                books[it].id
            },
            pageSize = PageSize.Fill
        ) { index ->
            val book = books[index]
            val imagePainter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(book.coverImageUrl)
                    .size(Size.ORIGINAL)
                    .build()
            )

            val imageState = imagePainter.state

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            //Navigate to detail movie screen
                            navigate(book)
                        }
                ) {
                    Box {
                        Box(
                            Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .clip(ImageBorderShape)
                                .background(MaterialTheme.colorApp.bookSurface)
                        ) {
                            ImageView(
                                imageModel = rememberResolvedBookImagePath(
                                    bookUrl = book.id,
                                    imagePath = book.coverImageUrl
                                ),
                                contentDescription = book.title,
                                modifier = Modifier.fillMaxSize(),
                                error = R.drawable.default_book_cover,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 12.dp,
                                top = 22.dp,
                            )
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                }
            }

            LaunchedEffect(true) {
                while(true){
                    delay(3000)
                    scope.launch {

                        if (pagerState.canScrollForward) {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        } else {
                            pagerState.animateScrollToPage(
                                0
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(books.size) { index ->
                    if (index == pagerState.currentPage) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    }
                    if (index != books.size - 1) {
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
    }
}