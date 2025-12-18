package com.hiendao.coreui.utils

import androidx.annotation.StringRes
import com.hiendao.coreui.R

/**
 * ISO 639-1 codes
 * https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
 */
enum class LanguageCode(
    @Suppress("PropertyName") val iso639_1: String,
    @StringRes val nameResId: Int
) {
    VIETNAMESE(iso639_1 = "en", nameResId = R.string.language_vietnam),
    ENGLISH(iso639_1 = "en", nameResId = R.string.language_english)
}