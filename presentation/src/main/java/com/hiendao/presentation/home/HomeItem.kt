package com.hiendao.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.hiendao.coreui.components.ImageView
import com.hiendao.coreui.theme.ColorFavourite
import com.hiendao.coreui.theme.ImageBorderShape
import com.hiendao.coreui.theme.colorApp
import com.hiendao.domain.model.Book
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import com.hiendao.presentation.R
import com.hiendao.coreui.R as CoreR
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItem(
    modifier: Modifier = Modifier,
    book: Book,
    onFavoriteClick: (Book) -> Unit = {},
    onPlayClick: (Book) -> Unit = {},
    onItemClick: (Book) -> Unit = {}
) {
    val context = LocalContext.current
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(book.coverImageUrl)
            .size(Size.ORIGINAL)
            .build()
    )

    val imageState = imagePainter.state
    val imageModel = rememberResolvedBookImagePath(
        bookUrl = book.id,
        imagePath = book.coverImageUrl
    )
    val indication: Indication = LocalIndication.current
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
        ){
            Box(
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1 / 1.45f)
                    .clip(ImageBorderShape)
                    .background(MaterialTheme.colorApp.bookSurface)
                    .combinedClickable(
                        indication = indication,
                        interactionSource = interactionSource,
                        role = Role.Button,
                        onClick = {
                            onItemClick.invoke(book)
                        },
                        onLongClick = {
                            onItemClick.invoke(book)
                        }
                    )
            ) {
                ImageView(
                    imageModel = imageModel,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    error = com.hiendao.coreui.R.drawable.default_book_cover,
                )
            }
//            Card(
//                modifier = Modifier.clickable {
//                    onItemClick.invoke(book)
//                }.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp),
//                elevation = CardDefaults.cardElevation(0.dp)
//            ) {
//                Box(modifier = Modifier
//                    .fillMaxWidth()
//                ) {
//                    when (imageState) {
//                        is AsyncImagePainter.State.Success -> {
//                            Image(
//                                bitmap = imageState.result.drawable.toBitmap().asImageBitmap(),
//                                contentDescription = "${book.title} poster",
//                                contentScale = ContentScale.Crop,
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .clip(RoundedCornerShape(10.dp))
//                                    .background(MaterialTheme.colorScheme.background)
//                            )
//                        }
//
//                        is AsyncImagePainter.State.Loading -> {
//                            CircularProgressIndicator(
//                                color = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier
//                                    .size(150.dp)
//                                    .align(Alignment.Center)
//                                    .scale(0.5f)
//                            )
//                        }
//
//                        else -> {
//                            ImageView(
//                                imageModel = imageModel,
//                                contentDescription = "No image",
//                                modifier = Modifier.fillMaxSize(),
//                                error = com.hiendao.coreui.R.drawable.default_book_cover,
//                            )
//                            Icon(
//                                imageVector = Icons.Default.BrokenImage,
//                                contentDescription = "No image",
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .clip(RoundedCornerShape(10.dp))
//                                    .background(MaterialTheme.colorScheme.background)
//                                    .padding(32.dp)
//                                    .alpha(0.8f),
//                                tint = MaterialTheme.colorScheme.onBackground
//                            )
//                        }
//                    }
//                }
//            }
            IconButton(
                onClick = {
                    onFavoriteClick.invoke(book)
                },
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = if(book.isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(CoreR.string.favourite),
                    tint = ColorFavourite
                )
            }
        }

        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            overflow = TextOverflow.Ellipsis
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(end = 4.dp).weight(1f).clip(RoundedCornerShape(6.dp)).border(1.dp, color = Color.Gray, shape = RoundedCornerShape(6.dp))
                    .clickable { onPlayClick.invoke(book) }.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = stringResource(CoreR.string.action_play),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(CoreR.string.action_play),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.padding(start = 4.dp).weight(1f).clip(RoundedCornerShape(6.dp)).border(1.dp, color = Color.Gray, shape = RoundedCornerShape(6.dp))
                    .clickable { onItemClick.invoke(book) }.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_read_book),
                    contentDescription = stringResource(CoreR.string.read),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(CoreR.string.str_read),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}