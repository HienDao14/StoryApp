package com.hiendao.storyapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.theme.InternalTheme
import com.hiendao.coreui.theme.Theme
import com.hiendao.coreui.theme.ThemeProvider
import com.hiendao.coreui.utils.LocaleHelper
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.domain.mockData.MockData
import com.hiendao.domain.model.*
import com.hiendao.domain.repository.BooksRepository
import com.hiendao.navigation.NavigationRoutes
import com.hiendao.presentation.appRoute.AppHomeScreen
import com.hiendao.storyapp.ui.theme.StoryAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.io.reader

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: BooksRepository

    @Inject
    internal lateinit var navigationRoutes: NavigationRoutes

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var themeProvider: ThemeProvider

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                MockData.insertMocksWithSql(appDatabase)
//            }
//        }
        setContent {
            Theme(themeProvider = themeProvider) {
                AppHomeScreen(
                    modifier = Modifier
                        .fillMaxSize(),
                    onBookOpen = { bookId, chapterUrl ->
                        openBookAtChapter(chapterUrl, bookId)
                    },
                    appPreferences
                )
            }
        }
        
        lifecycleScope.launch {
            appPreferences.APP_LANGUAGE.flow().collect {
                val currentLang = com.hiendao.coreui.utils.LocaleHelper.getLanguage(this@MainActivity)
                if (currentLang != it) {
                    LocaleHelper.setLocale(this@MainActivity, it)
                    recreate()
                }
            }
        }
    }

    fun openBookAtChapter(chapterUrl: String, bookId: String) = navigationRoutes.reader(
        this, bookUrl = bookId, chapterUrl = chapterUrl
    ).let(::startActivity)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoryAppTheme {
        // Preview content
    }
}
