package com.hiendao.presentation.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hiendao.coreui.R
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.textPadding
import com.hiendao.domain.translator.TranslationModelState

@Composable
internal fun SettingsTranslationModels(
    translationModelsStates: List<TranslationModelState>,
    onDownloadTranslationModel: (lang: String) -> Unit,
    onRemoveTranslationModel: (lang: String) -> Unit,
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    Column {
        Text(
            text = stringResource(R.string.settings_title_translation_models),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.textPadding(),
            color = ColorAccent
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.open_translation_models_manager))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.settings_translations_models_main_description))
            },
            leadingContent = {
                Icon(Icons.Outlined.Translate, null, tint = MaterialTheme.colorScheme.primary)
            },
            modifier = Modifier.clickable { isDialogVisible = true }
        )
    }
    SettingsTranslationModelsDialog(
        translationModelsStates = translationModelsStates,
        onDownloadTranslationModel = onDownloadTranslationModel,
        onRemoveTranslationModel = onRemoveTranslationModel,
        visible = isDialogVisible,
        setVisible = { isDialogVisible = it }
    )
}

