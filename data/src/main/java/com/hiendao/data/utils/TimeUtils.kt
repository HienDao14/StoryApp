package com.hiendao.data.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String.toMillisLegacy(): Long {
    return try {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        sdf.parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}
