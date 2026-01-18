@file:OptIn(ExperimentalMaterial3Api::class)
package com.hiendao.presentation.appRoute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.presentation.model.BottomItem

@Composable
fun AppHomeScreen(
    modifier: Modifier = Modifier,
    onBookOpen: ((bookId: String, chapterUrl: String) -> Unit)? = null,
    appPreferences: AppPreferences
) {
    val navController = rememberNavController()
    AppNavHost(navController, modifier, onBookOpen = onBookOpen, appPreferences)
}

@Composable
fun CenterText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}