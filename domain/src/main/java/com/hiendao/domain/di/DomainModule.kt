package com.hiendao.domain.di

import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.translator.TranslationManager
import com.hiendao.domain.translator.TranslationManagerMLKit
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {

    @Provides
    @Singleton
    fun provideTranslationManager(coroutineScope: AppCoroutineScope): TranslationManager {
        return TranslationManagerMLKit(coroutineScope)
    }

}