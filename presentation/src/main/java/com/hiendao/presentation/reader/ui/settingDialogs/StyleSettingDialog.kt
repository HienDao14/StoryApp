package com.hiendao.presentation.reader.ui.settingDialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.Themes
import com.hiendao.coreui.R
import com.hiendao.presentation.component.MySlider
import com.hiendao.presentation.reader.tools.FontsLoader
import com.hiendao.presentation.reader.ui.ReaderScreenState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StyleSettingDialog(
    state: ReaderScreenState.Settings.StyleSettingsData,
    onTextSizeChange: (Float) -> Unit,
    onTextFontChange: (String) -> Unit,
    onFollowSystemChange: (Boolean) -> Unit,
    onThemeChange: (Themes) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onTextAlignChange: (Int) -> Unit,
    onScreenMarginChange: (Int) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        // Text size
        var currentTextSize by remember { mutableFloatStateOf(state.textSize.value) }
        MySlider(
            value = currentTextSize,
            valueRange = 8f..32f,
            onValueChange = {
                currentTextSize = it
                onTextSizeChange(currentTextSize)
            },
            text = stringResource(R.string.text_size) + ": %.2f".format(currentTextSize),
            modifier = Modifier.padding(16.dp)
        )
        // Line Height
        var currentLineHeight by remember { mutableFloatStateOf(state.lineHeight.value) }
        MySlider(
            value = currentLineHeight,
            valueRange = 1.0f..3.0f,
            onValueChange = {
                currentLineHeight = it
                onLineHeightChange(currentLineHeight)
            },
            text = "Line Height: %.1f".format(currentLineHeight),
            modifier = Modifier.padding(16.dp)
        )
        // Screen Margin
        var currentMargin by remember { mutableFloatStateOf(state.screenMargin.value.toFloat()) }
        MySlider(
            value = currentMargin,
            valueRange = 0f..64f,
            onValueChange = {
                currentMargin = it
                onScreenMarginChange(currentMargin.toInt())
            },
            text = "Margin: ${currentMargin.toInt()} dp",
            modifier = Modifier.padding(16.dp)
        )
        // Alignment
        ListItem(
            modifier = Modifier.clickable { onTextAlignChange(if (state.textAlign.value == 0) 1 else 0) },
            headlineContent = { Text(stringResource(R.string.text_alignment)) },
            trailingContent = {
                 Text(if (state.textAlign.value == 0) "Left" else "Justify")
            }
        )
        // Text font
        Box {
            var showFontsDropdown by rememberSaveable { mutableStateOf(false) }
            val fontLoader = remember { FontsLoader() }
            var rowSize by remember { mutableStateOf(Size.Zero) }
            ListItem(
                modifier = Modifier
                    .clickable { showFontsDropdown = !showFontsDropdown }
                    .onGloballyPositioned { rowSize = it.size.toSize() },
                headlineContent = {
                    Text(
                        text = state.textFont.value,
                        fontFamily = fontLoader.getFontFamily(state.textFont.value),
                    )
                },
                leadingContent = { Icon(Icons.Filled.TextFields, null) },
                colors = ListItemDefaults.colors(
                    leadingIconColor = MaterialTheme.colorScheme.primary
                ),
            )
            DropdownMenu(
                expanded = showFontsDropdown,
                onDismissRequest = { showFontsDropdown = false },
                offset = DpOffset(0.dp, 10.dp),
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .width(with(LocalDensity.current) { rowSize.width.toDp() })
                    .background(MaterialTheme.colorScheme.background)
            ) {
                FontsLoader.availableFonts.forEach { item ->
                    DropdownMenuItem(
                        onClick = { onTextFontChange(item) },
                        text = {
                            Text(
                                text = item,
                                fontFamily = fontLoader.getFontFamily(item),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
        }
        // Follow system theme
        ListItem(
            modifier = Modifier
                .clickable { onFollowSystemChange(!state.followSystem.value) },
            headlineContent = {
                Text(text = stringResource(id = R.string.follow_system))
            },
            leadingContent = {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = state.followSystem.value,
                    onCheckedChange = onFollowSystemChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedBorderColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            },
            colors = ListItemDefaults.colors(
                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        )
        if(!state.followSystem.value){
            // Themes
            ListItem(
                headlineContent = {
                    FlowRow(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Themes.entries.forEach {
                            FilterChip(
                                selected = it == state.currentTheme.value,
                                onClick = { onThemeChange(it) },
                                label = { Text(text = stringResource(id = it.nameId)) }
                            )
                        }
                    }
                },
                leadingContent = {
                    Icon(
                        Icons.Outlined.ColorLens,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = ListItemDefaults.colors(
                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            )
        }
    }
}
