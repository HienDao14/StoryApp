package com.hiendao.presentation.search

import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.Toasty
import com.hiendao.domain.model.Book
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.utils.Response
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

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentPage = 0
    private val _isEndReached = MutableStateFlow(false)
    val isEndReached = _isEndReached.asStateFlow()

    fun searchBooks(isLoadMore: Boolean = false) {
        if(query.value.isBlank()) return
        viewModelScope.launch {
            if (!isLoadMore) {
                currentPage = 0
                _isEndReached.emit(false)
                _isLoading.emit(true)
            }

            appRepository.libraryBooks.searchBooks(query.value, if(isLoadMore) currentPage + 1 else 0).collect { result ->
                when(result){
                    is Response.Loading -> {
                        if (!isLoadMore) _isLoading.emit(true)
                    }
                    is Response.Success -> {
                        _isLoading.emit(false)
                        _error.emit(null)
                        
                        if (isLoadMore) currentPage++ else currentPage = 0

                        if(result.data.isEmpty()){
                            if (!isLoadMore) {
                                _searchList.emit(emptyList())
                                _error.emit("No results found for \"${query.value}\"")
                            }
                            _isEndReached.emit(true)
                        } else {
                            if (isLoadMore) {
                                val currentList = _searchList.value
                                val newList = mergeBooks(currentList, result.data)
                                _searchList.value = newList
                            } else {
                                _searchList.value = result.data
                            }
                            if (result.data.isEmpty()) _isEndReached.emit(true)
                        }
                    }
                    is Response.Error -> {
                        _isLoading.emit(false)
                        if (!isLoadMore) {
                            _searchList.emit(emptyList())
                            _error.emit(result.message ?: "An unexpected error occurred")
                        }
                    }
                    is Response.None -> Unit
                }
            }
        }
    }

    private fun mergeBooks(current: List<Book>, new: List<Book>): List<Book> {
        val newIds = new.map { it.id }.toSet()
        return (current.filter { it.id !in newIds } + new)
    }

    fun onSearchInputChange(input: String) {
        viewModelScope.launch {
            _query.emit(input)
        }
    }

    fun onSearchInputSubmit(input: String){
        viewModelScope.launch {
            _query.emit(input)
            _query.emit(input)
            searchBooks(isLoadMore = false)
        }
    }

}