package com.hiendao.data.remote.retrofit.category.model

data class CategoryDTO(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null
)
