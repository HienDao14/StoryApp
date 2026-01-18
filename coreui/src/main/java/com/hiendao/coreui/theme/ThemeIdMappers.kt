package com.hiendao.coreui.theme

import com.hiendao.coreui.appPreferences.PreferenceThemes


val PreferenceThemes.toTheme
    get() = when (this) {
        PreferenceThemes.Light -> Themes.LIGHT
        PreferenceThemes.Dark -> Themes.DARK
        PreferenceThemes.Black -> Themes.DARK
    }

val Themes.toPreferenceTheme
    get() = when (this) {
        Themes.LIGHT -> PreferenceThemes.Light
        Themes.DARK -> PreferenceThemes.Dark
//        Themes.BLACK -> PreferenceThemes.Black
    }