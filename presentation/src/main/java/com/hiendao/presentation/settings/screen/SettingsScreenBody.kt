package com.hiendao.presentation.settings.screen


import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.R
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.InternalTheme
import com.hiendao.coreui.theme.Themes
import com.hiendao.coreui.theme.textPadding
import com.hiendao.presentation.settings.state.SettingsScreenState

@Composable
internal fun SettingsScreenBody(
    state: SettingsScreenState,
    modifier: Modifier = Modifier,
    onFollowSystem: (Boolean) -> Unit,
    onThemeSelected: (Themes) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onDownloadTranslationModel: (lang: String) -> Unit,
    onRemoveTranslationModel: (lang: String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentAppVersion = context.getVersionName()
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        SettingsTheme(
            currentFollowSystem = state.followsSystemTheme.value,
            currentTheme = state.currentTheme.value,
            onFollowSystemChange = onFollowSystem,
            onCurrentThemeChange = onThemeSelected
        )
        // Language Selection
        val languageMap = mapOf(
            "vi" to stringResource(R.string.lang_vi),
            "en" to stringResource(R.string.lang_en),
            "zh" to stringResource(R.string.lang_zh)
        )
        val showLanguageDialog = remember { mutableStateOf(false) }
        
        ListItem(
            modifier = Modifier.clickable { showLanguageDialog.value = true },
            headlineContent = { Text(text = stringResource(R.string.language)) },
            supportingContent = { 
                Text(text = languageMap[state.currentLanguage.value] ?: state.currentLanguage.value) 
            },
            leadingContent = {
                Icon(Icons.Outlined.Translate, null, tint = MaterialTheme.colorScheme.primary)
            }
        )

        if (showLanguageDialog.value) {
            AlertDialog(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = { showLanguageDialog.value = false },
                title = { Text(text = stringResource(R.string.choose_language)) },
                text = {
                    Column {
                        languageMap.forEach { (code, name) ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    onLanguageSelected(code)
                                    showLanguageDialog.value = false
                                },
                                headlineContent = { Text(text = name) },
                                leadingContent = {
                                    androidx.compose.material3.RadioButton(
                                        selected = state.currentLanguage.value == code,
                                        onClick = null
                                    )
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showLanguageDialog.value = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
        if (state.isTranslationSettingsVisible.value) {
            HorizontalDivider()
            SettingsTranslationModels(
                translationModelsStates = state.translationModelsStates,
                onDownloadTranslationModel = onDownloadTranslationModel,
                onRemoveTranslationModel = onRemoveTranslationModel
            )
        }
        HorizontalDivider()
        Text(
            text = stringResource(R.string.app_updates) + " | " + currentAppVersion,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.textPadding()
        )
        HorizontalDivider()
        val showLogoutDialog = remember { mutableStateOf(false) }

        if (showLogoutDialog.value) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog.value = false },
                title = { Text(text = stringResource(R.string.logout_confirmation_title)) },
                text = { Text(text = stringResource(R.string.logout_confirmation_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog.value = false
                            onLogout()
                        }
                    ) {
                        Text(stringResource(R.string.log_out))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLogoutDialog.value = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }

        ListItem(
            modifier = Modifier
                .clickable { showLogoutDialog.value = true },
            headlineContent = {
                Text(text = stringResource(id = R.string.log_out))
            },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {

            }
        )
        Spacer(modifier = Modifier.height(500.dp))
        Text(
            text = "(°.°)",
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(120.dp))
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val isDark = isSystemInDarkTheme()
    val theme = remember { mutableStateOf(if (isDark) Themes.DARK else Themes.LIGHT) }
    InternalTheme(theme.value) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreenBody(
                state = SettingsScreenState(
                    followsSystemTheme = remember { derivedStateOf { true } },
                    currentTheme = theme,
                    isTranslationSettingsVisible = remember { mutableStateOf(true) },
                    translationModelsStates = remember { mutableStateListOf() },
                    currentLanguage = remember { mutableStateOf("en") }
                ),
                onFollowSystem = { },
                onThemeSelected = { },
                onLanguageSelected = { },
                onDownloadTranslationModel = { },
                onRemoveTranslationModel = { }
            )
        }
    }
}

@Suppress("DEPRECATION")
fun Context.getPackageInfo(): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
}

fun Context.getVersionName(): String = try {
    getPackageInfo().versionName.toString()
} catch (e: PackageManager.NameNotFoundException) {
    ""
} catch (e : Exception) {
    ""
}