package com.hiendao.coreui.di

import com.hiendao.coreui.theme.AppThemeProvider
import com.hiendao.coreui.theme.ThemeProvider
import com.hiendao.coreui.utils.Toasty
import com.hiendao.coreui.utils.ToastyToast
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class CoreUIModule {

    @Binds
    @Singleton
    internal abstract fun themeProviderBinder(appThemeProvider: AppThemeProvider): ThemeProvider

    @Binds
    @Singleton
    abstract fun bindToasty(toast: ToastyToast): Toasty
}