package com.hiendao.data.remote.retrofit.category

import com.hiendao.data.remote.retrofit.category.model.ListCategoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryApi {
    @GET("")
    suspend fun getAllCategory(): ListCategoryResponse
}