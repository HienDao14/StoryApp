package com.hiendao.data.remote.retrofit.book.model

data class Content(
    val author: String,
    val coverImageUrl: String?=null,
    val id: String,
    val isFavorite: Boolean,
    val status: String,
    val title: String,
    val webView: Boolean
)