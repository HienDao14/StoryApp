package com.hiendao.data.remote.retrofit.book

import com.hiendao.data.remote.retrofit.book.model.BookResponse
import com.hiendao.data.remote.retrofit.book.model.BookResponseDTO
import com.hiendao.data.remote.retrofit.book.model.BooksResponseDTO
import com.hiendao.data.remote.retrofit.book.model.ListBookResponse
import com.hiendao.data.remote.retrofit.book.model.SearchBooksBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {

    @GET("stories")
    suspend fun getBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate"
    ): BooksResponseDTO

    @GET("stories")
    suspend fun getNewestBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate"
    ): BooksResponseDTO

    @GET("stories/favorites")
    suspend fun getFavouriteBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate"
    ): BooksResponseDTO

    @GET("stories/recently-read")
    suspend fun getRecentlyReadBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate"
    ): BooksResponseDTO

    @GET("stories/my-library")
    suspend fun getLibraryBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate"
    ): BooksResponseDTO

    @GET("")
    suspend fun getBookOfCategory(
        @Query("categoryId") categoryId: String,
        @Query("page") page: Int?
    ): ListBookResponse

    @GET("stories/{id}")
    suspend fun getBookDetail(
        @Path("id") bookId: String
    ): BookResponseDTO

    @POST("stories/search")
    suspend fun searchBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createDate",
        @Body searchBody: SearchBooksBody
    ): BooksResponseDTO
}