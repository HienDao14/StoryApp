package com.hiendao.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hiendao.coreui.R
import com.hiendao.coreui.components.TopAppBarSearch
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.domain.model.Book
import com.hiendao.presentation.search.screen.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    onBookClick: (Book) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val searchList = searchViewModel.searchList.collectAsStateWithLifecycle()
    var searchInput = searchViewModel.query.collectAsStateWithLifecycle()
    var isLoading = searchViewModel.isLoading.collectAsStateWithLifecycle()
    var isEndReached = searchViewModel.isEndReached.collectAsStateWithLifecycle()
    var error = searchViewModel.error.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBarSearch(
                    focusRequester = focusRequester,
                    searchTextInput = searchInput.value,
                    onSearchTextChange = searchViewModel::onSearchInputChange,
                    onTextDone = searchViewModel::onSearchInputSubmit,
                    onClose = onBackClick,
                    placeholderText = stringResource(R.string.global_search),
                    scrollBehavior = scrollBehavior,
                )

                if(isLoading.value == true && !searchList.value.isNotEmpty()){
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = ColorAccent
                    )
                }

                if(!error.value.isNullOrEmpty()){
                    Text(
                        text = error.value!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        },
        content = { innerPadding ->
            SearchScreen(
                list = searchList.value,
                modifier = Modifier.padding(innerPadding),
                onClick = {
                    onBookClick
                },
                onLoadMore = {
                    searchViewModel.searchBooks(isLoadMore = true)
                },
                isLoading = isLoading.value,
                isEndReached = isEndReached.value
            )
        }
    )

}