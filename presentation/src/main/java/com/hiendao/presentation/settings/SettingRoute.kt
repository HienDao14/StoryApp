package com.hiendao.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiendao.coreui.R
import com.hiendao.coreui.components.CollapsibleDivider
import com.hiendao.presentation.settings.screen.SettingsScreenBody
import com.hiendao.presentation.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingRoute(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = { }
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Unspecified,
                        scrolledContainerColor = Color.Unspecified,
                    ),
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_settings),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                )
                CollapsibleDivider(scrollBehavior.state)
            }
        },
        content = { innerPadding ->
            SettingsScreenBody(
                state = viewModel.state,
                onFollowSystem = viewModel::onFollowSystemChange,
                onThemeSelected = viewModel::onThemeChange,
                onDownloadTranslationModel = viewModel::downloadTranslationModel,
                onRemoveTranslationModel = viewModel::removeTranslationModel,
                modifier = Modifier.padding(innerPadding),
                onLogout = onLogout
            )
        }
    )
}

