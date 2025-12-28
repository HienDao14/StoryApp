package com.hiendao.presentation.utils

import android.text.Html
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat

fun String.parseHtml(): AnnotatedString {
    val spanned: Spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    return buildAnnotatedString {
        append(spanned.toString())
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> {
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        }
                        android.graphics.Typeface.ITALIC -> {
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        }
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                ), start, end
                            )
                        }
                    }
                }
                is UnderlineSpan -> {
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                }
                is StrikethroughSpan -> {
                    addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                }
                is ForegroundColorSpan -> {
                    addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }
            }
        }
    }
}
