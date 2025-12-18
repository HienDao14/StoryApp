package com.hiendao.coreui.utils

import androidx.compose.ui.state.ToggleableState

fun TernaryState.toToggleableState() = when (this) {
    TernaryState.Active -> ToggleableState.On
    TernaryState.Inverse -> ToggleableState.Indeterminate
    TernaryState.Inactive -> ToggleableState.Off
}