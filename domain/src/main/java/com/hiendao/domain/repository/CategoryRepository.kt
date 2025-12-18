package com.hiendao.domain.repository

import com.hiendao.data.remote.retrofit.category.CategoryApi
import com.hiendao.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApi: CategoryApi
) {
    suspend fun getAllCategories(): List<Category> {
        return try {
            categoryApi.getAllCategory().results.map { dto ->
                Category(
                    id = dto.id,
                    name = dto.name,
                    iconUrl = dto.iconUrl
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
