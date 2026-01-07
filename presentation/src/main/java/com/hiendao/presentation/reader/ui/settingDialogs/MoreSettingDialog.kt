package com.hiendao.presentation.reader.ui.settingDialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.presentation.component.MySlider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.R

@Composable
internal fun MoreSettingDialog(
    allowTextSelection: Boolean,
    onAllowTextSelectionChange: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOn: (Boolean) -> Unit,
    fullScreen: Boolean,
    onFullScreen: (Boolean) -> Unit,
    brightness: Float,
    onBrightnessChanged: (Float) -> Unit,
    nightMode: Boolean,
    onNightModeChanged: (Boolean) -> Unit,
    autoScrollSpeed: Int,
    onAutoScrollSpeedChanged: (Int) -> Unit,
    volumeKeyNavigation: Boolean,
    onVolumeKeyNavigationChanged: (Boolean) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        // Allow text selection
        ListItem(
            modifier = Modifier
                .clickable { onAllowTextSelectionChange(!allowTextSelection) },
            headlineContent = {
                Text(text = stringResource(id = R.string.allow_text_selection))
            },
            leadingContent = {
                Icon(
                    Icons.Outlined.TouchApp,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = allowTextSelection,
                    onCheckedChange = onAllowTextSelectionChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedBorderColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        )
        // Keep screen on
        ListItem(
            modifier = Modifier
                .clickable { onKeepScreenOn(!keepScreenOn) },
            headlineContent = {
                Text(text = stringResource(R.string.keep_screen_on))
            },
            leadingContent = {
                Icon(
                    Icons.Outlined.LightMode,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOn,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedBorderColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        )
        // Keep screen on
        ListItem(
            modifier = Modifier
                .clickable { onFullScreen(!fullScreen) },
            headlineContent = {
                Text(text = stringResource(R.string.features_reader_full_screen))
            },
            leadingContent = {
                Icon(
                    Icons.Outlined.Fullscreen,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = fullScreen,
                    onCheckedChange = onFullScreen,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedBorderColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        )
        // Brightness
        var currentBrightness by remember(brightness) { mutableFloatStateOf(brightness) }
        MySlider(
            value = if (currentBrightness < 0) 0.5f else currentBrightness,
            valueRange = 0f..1f,
            onValueChange = {
                currentBrightness = it
                onBrightnessChanged(currentBrightness)
            },
            text = "Brightness: ${if (brightness < 0) "System" else "%.0f%%".format(brightness * 100)}",
            modifier = Modifier.padding(16.dp)
        )
        // Night Mode
        ListItem(
            modifier = Modifier.clickable { onNightModeChanged(!nightMode) },
            headlineContent = { Text("Night Mode (Blue Light Filter)") },
            trailingContent = {
                Switch(
                    checked = nightMode,
                    onCheckedChange = onNightModeChanged
                )
            }
        )
        // Volume Key Nav
        ListItem(
            modifier = Modifier.clickable { onVolumeKeyNavigationChanged(!volumeKeyNavigation) },
            headlineContent = { Text("Volume Key Navigation") },
            trailingContent = {
                Switch(
                    checked = volumeKeyNavigation,
                    onCheckedChange = onVolumeKeyNavigationChanged
                )
            }
        )
         // Auto Scroll
        var currentAutoScroll by remember(autoScrollSpeed) { mutableFloatStateOf(autoScrollSpeed.toFloat()) }
        MySlider(
            value = currentAutoScroll,
            valueRange = 0f..50f,
            onValueChange = {
                currentAutoScroll = it
                onAutoScrollSpeedChanged(currentAutoScroll.toInt())
            },
            text = "Auto Scroll Speed: ${currentAutoScroll.toInt()}",
            modifier = Modifier.padding(16.dp)
        )
    }
}