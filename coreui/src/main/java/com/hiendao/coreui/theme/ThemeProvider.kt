package com.hiendao.coreui.theme

import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.State

interface ThemeProvider {

    fun followSystem(stateCoroutineScope: CoroutineScope): State<Boolean>

    fun currentTheme(stateCoroutineScope: CoroutineScope): State<Themes>
}