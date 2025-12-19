package com.hiendao.coreui.utils

import java.text.Normalizer
import java.util.regex.Pattern

object StringUtils {
    fun removeAccents(str: String): String {
        val nfdNormalizedString: String = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace('đ', 'd').replace('Đ', 'D')
    }
}
