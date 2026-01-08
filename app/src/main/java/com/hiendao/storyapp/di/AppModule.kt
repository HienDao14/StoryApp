package com.hiendao.storyapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.data.local.dao.ChapterBodyDao
import com.hiendao.data.local.dao.ChapterDao
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.storyapp.di.MyInterceptor
import com.hiendao.data.remote.retrofit.book.BookApi
import com.hiendao.data.remote.retrofit.chapter.ChapterApi
import com.hiendao.data.remote.retrofit.login.LoginAPI
import com.hiendao.data.remote.retrofit.story.StoryApi
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.repository.BooksRepository
import com.hiendao.domain.repository.StoryRepository
import com.hiendao.domain.repository.StoryRepositoryImpl
import com.hiendao.data.remote.retrofit.voice.VoiceApi
import com.hiendao.domain.repository.LoginRepository
import com.hiendao.domain.repository.VoiceRepository
import com.hiendao.domain.repository.VoiceRepositoryImpl
import com.hiendao.navigation.NavigationRoutes
import com.hiendao.storyapp.AppNavigationRoutes
import com.hiendao.storyapp.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "appdb.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideLibraryDao(db: AppDatabase) = db.libraryDao

    @Singleton
    @Provides
    fun provideChapterDao(db: AppDatabase) = db.chapterDao

    @Singleton
    @Provides
    fun provideChapterBodyDao(db: AppDatabase) = db.chapterBodyDao

    @Provides
    @Singleton
    fun provideAppCoroutineScope(): AppCoroutineScope {
        return object : AppCoroutineScope {
            override val coroutineContext =
                SupervisorJob() + Dispatchers.Main.immediate + CoroutineName("App")
        }
    }

    @Provides
    fun provideMyInterceptor(appPreferences: AppPreferences): MyInterceptor{
        return MyInterceptor(appPreferences)
    }

    @Provides
    fun provideOkHttpClient(myInterceptor: MyInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(myInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .readTimeout(600000L, java.util.concurrent.TimeUnit.MILLISECONDS)
            .connectTimeout(30000L, java.util.concurrent.TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideLoginApi(retrofit: Retrofit): LoginAPI {
        return retrofit.create(LoginAPI::class.java)
    }

    @Provides
    fun provideBookApi(retrofit: Retrofit): BookApi {
        return retrofit.create(BookApi::class.java)
    }

    @Provides
    fun provideChapterApi(retrofit: Retrofit): ChapterApi {
        return retrofit.create(ChapterApi::class.java)
    }

    @Provides
    fun provideStoryApi(retrofit: Retrofit): StoryApi {
        return retrofit.create(StoryApi::class.java)
    }

    @Provides
    fun provideVoiceApi(retrofit: Retrofit): VoiceApi {
        return retrofit.create(VoiceApi::class.java)
    }

    @Provides
    fun provideCategoryApi(retrofit: Retrofit): com.hiendao.data.remote.retrofit.category.CategoryApi {
        return retrofit.create(com.hiendao.data.remote.retrofit.category.CategoryApi::class.java)
    }



    @Provides
    @Singleton
    fun bindAppNavigationRoutes(nav: AppNavigationRoutes): NavigationRoutes{
        return nav
    }

    @Provides
    @Singleton
    fun provideNovelRepository(
        @ApplicationContext context: Context
    ): BooksRepository = BooksRepository(context)

    @Provides
    @Singleton
    fun provideLoginRepository(
        loginAPI: LoginAPI
    ): LoginRepository = LoginRepository(loginAPI)

    @Provides
    @Singleton
    fun provideStoryRepository(
        storyApi: StoryApi,
        libraryDao: LibraryDao,
        chapterDao: ChapterDao,
        chapterBodyDao: ChapterBodyDao
    ): StoryRepository = StoryRepositoryImpl(storyApi, libraryDao, chapterDao, chapterBodyDao)

    @Provides
    @Singleton
    fun provideVoiceRepository(
        voiceApi: VoiceApi
    ): VoiceRepository = VoiceRepositoryImpl(voiceApi)
}