package com.hiendao.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiendao.presentation.model.BottomItem

private val bottomItems = listOf(BottomItem.Home, BottomItem.Library, BottomItem.Settings)
/* ---------- Main scaffold có BottomBar ---------- */
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = if(currentRoute == item.route) item.selectedIcon else item.icon,
                        label = {
                            Text(
                                when(item.label){
                                    "Home" -> stringResource(id = com.hiendao.coreui.R.string.title_home)
                                    "Library" -> stringResource(id = com.hiendao.coreui.R.string.title_library)
                                    "Settings" -> stringResource(id = com.hiendao.coreui.R.string.title_settings)
                                    else -> item.label
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(bottom = inner.calculateBottomPadding())) {
            content()
        }
    }
}
