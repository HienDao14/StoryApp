package com.hiendao.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable


sealed class BottomItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
    val selectedIcon: @Composable () -> Unit = icon
) {
    data object Home : BottomItem("home", "Home", { Icon(Icons.Outlined.Home, contentDescription = "Home") }, { Icon(Icons.Filled.Home, contentDescription = "Home") })
    data object Library : BottomItem("library", "Library", { Icon(Icons.Outlined.LibraryBooks, contentDescription = "Library") }, { Icon(Icons.Filled.LibraryBooks, contentDescription = "Library") })
    data object Settings : BottomItem("settings", "Settings", { Icon(Icons.Outlined.Settings, contentDescription = "Settings") }, { Icon(Icons.Filled.Settings, contentDescription = "Settings") })
}
