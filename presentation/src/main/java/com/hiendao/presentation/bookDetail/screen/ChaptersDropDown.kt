package com.hiendao.presentation.bookDetail.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hiendao.coreui.R

@Composable
internal fun ChaptersDropDown(
    onResumeReading: () -> Unit,
    onChangeCover: () -> Unit,
) {
    DropdownMenuItem(
        onClick = onResumeReading,
        text = {
            Text(text = stringResource(id = R.string.resume_reading))
        },
        leadingIcon = {
            Icon(
                Icons.Filled.PlayArrow,
                stringResource(R.string.resume_reading),
            )
        }
    )
    DropdownMenuItem(
        onClick = onChangeCover,
        text = {
            Text(text = stringResource(R.string.change_cover))
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Image,
                stringResource(R.string.change_cover),
            )
        }
    )
}