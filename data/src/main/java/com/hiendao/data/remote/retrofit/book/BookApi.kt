package com.hiendao.data.remote.retrofit.book

import com.hiendao.data.remote.retrofit.book.model.BookResponse
import com.hiendao.data.remote.retrofit.book.model.ListBookResponse
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface BookApi {

    @GET("")
    suspend fun getBooks(
        @Query("page") page: Int?
    ): ListBookResponse

    @GET("")
    suspend fun getNewestBooks(
        @Query("page") page: Int?
    ): ListBookResponse

    @GET("")
    suspend fun getFeatureBooks(): ListBookResponse

    @GET("")
    suspend fun getFavouriteBooks(
        @Query("page") page: Int?
    ): ListBookResponse

    @GET("")
    suspend fun getBookOfCategory(
        @Query("categoryId") categoryId: String,
        @Query("page") page: Int?
    ): ListBookResponse

    @GET("")
    suspend fun getBookDetail(
        @Query("bookId") bookId: String
    ): BookResponse

    @PUT("")
    suspend fun updateBook(): BookResponse
}