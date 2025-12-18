package com.hiendao.storyapp

import android.content.Context
import android.content.Intent
import com.hiendao.data.local.entity.BookMetadata
import com.hiendao.navigation.NavigationRoutes
import com.hiendao.presentation.reader.ReaderActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java


@Singleton
class AppNavigationRoutes @Inject constructor() : NavigationRoutes {

    override fun main(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun reader(
        context: Context,
        bookUrl: String,
        chapterUrl: String,
        scrollToSpeakingItem: Boolean
    ): Intent {
        return ReaderActivity.IntentData(
            context,
            bookUrl = bookUrl,
            chapterUrl = chapterUrl,
            scrollToSpeakingItem = scrollToSpeakingItem
        )
    }

    override fun chapters(context: Context, bookMetadata: BookMetadata): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun databaseSearch(
        context: Context,
        input: String,
        databaseUrlBase: String
    ): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun databaseSearch(
        context: Context,
        databaseBaseUrl: String
    ): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun sourceCatalog(
        context: Context,
        sourceBaseUrl: String,
    ): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun globalSearch(
        context: Context,
        text: String,
    ): Intent {
        return Intent(context, MainActivity::class.java)
    }

    override fun webView(context: Context, url: String): Intent {
        return Intent(context, MainActivity::class.java)
    }

}

//@Singleton
//class AppNavigationRoutesViewModel @Inject constructor(
//    private val appNavigationRoutes: AppNavigationRoutes
//) : NavigationRouteViewModel(), NavigationRoutes by appNavigationRoutes