package com.hiendao.presentation.search

import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.Toasty
import com.hiendao.domain.model.Book
import com.hiendao.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferences: AppPreferences,
    private val toasty: Toasty
) : BaseViewModel() {

    private val _searchList = MutableStateFlow<List<Book>>(emptyList())
    val searchList = _searchList.asStateFlow()

    private val _query = MutableStateFlow<String>("")
    val query = _query.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun searchBooks() {
        if(query.value.isBlank()) return
        viewModelScope.launch {
            _isLoading.emit(true)

            appRepository.libraryBooks.searchBooks(query.value).collect {
                _isLoading.emit(false)
                _searchList.value = it
            }
        }
    }

    fun onSearchInputChange(input: String) {
        viewModelScope.launch {
            _query.emit(input)
        }
    }

    fun onSearchInputSubmit(input: String){
        viewModelScope.launch {
            _query.emit(input)
            searchBooks()
        }
    }

}