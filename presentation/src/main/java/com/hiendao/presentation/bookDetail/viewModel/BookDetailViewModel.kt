package com.hiendao.presentation.bookDetail.viewModel

import androidx.lifecycle.ViewModel
import com.hiendao.domain.repository.BooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): ViewModel() {

}