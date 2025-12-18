package com.hiendao.presentation.categoryDetail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hiendao.coreui.components.BookImageButtonView
import com.hiendao.coreui.modifiers.bounceOnPressed
import com.hiendao.domain.model.Book
import com.hiendao.domain.utils.rememberResolvedBookImagePath
import com.hiendao.presentation.categoryDetail.viewModel.CategoryDetailViewModel

@Composable
fun CategoryDetailRoute(
    onBackClick: () -> Unit,
    onBookClick: (Book) -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    CategoryDetailScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onBookClick = onBookClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onBackClick: () -> Unit,
    onBookClick: (Book) -> Unit
) {
    val books by viewModel.books.collectAsState()
    val categoryName by viewModel.categoryName.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 4.dp,
                start = 4.dp,
                end = 4.dp
            ),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            items(
                items = books,
                key = { it.id }
            ) { book ->
                val interactionSource = remember { MutableInteractionSource() }
                Box {
                    BookImageButtonView(
                        title = book.title,
                        coverImageModel = rememberResolvedBookImagePath(
                            bookUrl = book.id,
                            imagePath = book.coverImageUrl
                        ),
                        onClick = { onBookClick(book) },
                        onLongClick = { /* Optional: Handle long click if needed */ },
                        interactionSource = interactionSource,
                        modifier = Modifier.bounceOnPressed(interactionSource)
                    )
                }
            }
        }
    }
}
