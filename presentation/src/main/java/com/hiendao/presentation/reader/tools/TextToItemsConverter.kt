package com.hiendao.presentation.reader.tools

import com.hiendao.coreui.utils.BookTextMapper
import com.hiendao.domain.algorithms.delimiterAwareTextSplitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import com.hiendao.presentation.reader.domain.ImgEntry
import com.hiendao.presentation.reader.domain.ReaderItem

internal suspend fun textToItemsConverter(
    chapterUrl: String,
    chapterIndex: Int,
    chapterItemPositionDisplacement: Int,
    text: String
): List<ReaderItem> = withContext(Dispatchers.Default) {
    if (isHtml(text)) {
        return@withContext htmlToItemsConverter(
            chapterUrl = chapterUrl,
            chapterIndex = chapterIndex,
            chapterItemPositionDisplacement = chapterItemPositionDisplacement,
            text = text
        )
    }

    val paragraphs = text
        .splitToSequence("\n\n")
        .filter { it.isNotBlank() }
        .flatMap {
            delimiterAwareTextSplitter(
                fullText = it.trim(),
                maxSliceLength = 512,
                charDelimiter = '.'
            )
        }
        .toList()

    return@withContext paragraphs
        .mapIndexed { position, paragraph ->
            async {
                generateITEM(
                    chapterUrl = chapterUrl,
                    chapterIndex = chapterIndex,
                    chapterItemPosition = position + chapterItemPositionDisplacement,
                    text = paragraph,
                    location = when (position) {
                        0 -> ReaderItem.Location.FIRST
                        paragraphs.lastIndex -> ReaderItem.Location.LAST
                        else -> ReaderItem.Location.MIDDLE
                    }
                )
            }
        }.awaitAll()
}

private fun isHtml(text: String): Boolean {
    val checkRange = text.take(1000)
    return checkRange.contains("<p") ||
            checkRange.contains("<div") ||
            checkRange.contains("<br") ||
            checkRange.contains("<img") ||
            text.contains("</html>")
}

private fun generateITEM(
    chapterUrl: String,
    chapterIndex: Int,
    chapterItemPosition: Int,
    text: String,
    location: ReaderItem.Location
): ReaderItem = when (val imgEntry = BookTextMapper.ImgEntry.fromXMLString(text)) {
    null -> ReaderItem.Body(
        chapterUrl = chapterUrl,
        chapterIndex = chapterIndex,
        chapterItemPosition = chapterItemPosition,
        text = text,
        location = location
    )
    else -> ReaderItem.Image(
        chapterUrl = chapterUrl,
        chapterIndex = chapterIndex,
        chapterItemPosition = chapterItemPosition,
        text = text,
        location = location,
        image = ImgEntry(path = imgEntry.path, yrel = imgEntry.yrel)
    )
}
