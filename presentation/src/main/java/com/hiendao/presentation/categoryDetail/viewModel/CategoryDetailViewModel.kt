package com.hiendao.presentation.categoryDetail.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.domain.model.Book
import com.hiendao.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val appRepository: AppRepository
): ViewModel() {

    private var categoryId: String = ""
    private val _categoryName = MutableStateFlow<String>("")
    val categoryName = _categoryName.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books = _books.asStateFlow()

    fun updateState(categoryId: String, categoryName: String) {
        this.categoryId = categoryId
        _categoryName.value = categoryName
        getListCategoryDetail()
    }

    private fun getListCategoryDetail() {
        if (categoryId.isEmpty()) return
        viewModelScope.launch {
            val result = when (categoryId) {
                "favourite" -> appRepository.libraryBooks.getFavoriteBooksNormal()
                "newest" -> appRepository.libraryBooks.getNewestBooksNormal()
                "recentlyRead" -> appRepository.libraryBooks.getRecentlyReadBooksNormal()
                else -> appRepository.libraryBooks.getBooksByCategory(categoryId)
            }
            _books.emit(result)
        }
    }
}