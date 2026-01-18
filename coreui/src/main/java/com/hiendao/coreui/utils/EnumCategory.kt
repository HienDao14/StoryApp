package com.hiendao.coreui.utils

import androidx.annotation.StringRes
import com.hiendao.coreui.R

enum class StoryCategory {
    THE_THAO,
    VO_THUAT,
    HAI_HUOC,
    KHOA_HOC_VIEN_TUONG,
    CO_TICH,
    KHOA_HOC,
    GIA_TUONG
}

@StringRes
fun categoryStringToRes(category: String): Int? {
    return when (category) {
        "THE_THAO" -> R.string.category_the_thao
        "VO_THUAT" -> R.string.category_vo_thuat
        "HAI_HUOC" -> R.string.category_hai_huoc
        "KHOA_HOC_VIEN_TUONG" -> R.string.category_khoa_hoc_vien_tuong
        "CO_TICH" -> R.string.category_co_tich
        "KHOA_HOC" -> R.string.category_khoa_hoc
        "GIA_TUONG" -> R.string.category_gia_tuong
        else -> null
    }
}