package com.hiendao.domain.map

import com.hiendao.domain.model.Chapter
import com.hiendao.domain.model.Location
import com.hiendao.domain.model.ReaderItem
import com.hiendao.domain.model.ReaderTextItem
import com.hiendao.domain.model.TextType

private const val MAX_CHARS_PER_CHUNK = 60
fun Chapter.toReaderItems(): List<ReaderItem> {
    val result = mutableListOf<ReaderItem>()
    var orderCounter = 1

    // 1) Title của chương
    result += ReaderTextItem(
        id = "item_${id}_title",
        chapterId = id,
        order = orderCounter++,
        text = title.orIfBlank(""),
        type = TextType.TITLE,
        location = Location.FIRST
    )

    // 2) Nội dung (tách theo đoạn)
    // - Tách bằng 1 hoặc nhiều dòng trống
    val rawParagraphs = content
        .normalizeLineBreaks()
        .split(Regex("""\n\s*\n+"""))      // một hoặc nhiều xuống dòng trống
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    // 3) Chunk đoạn dài để tránh item quá lớn
    val chunks = rawParagraphs.flatMap { para ->
        chunkByLength(para, MAX_CHARS_PER_CHUNK)
    }

    // 4) Gán location cho từng chunk
    chunks.forEachIndexed { idx, chunk ->
        val location = when {
            chunks.size == 1 -> Location.LAST      // chỉ 1 chunk nội dung
            idx == 0 -> Location.FIRST
            idx == chunks.lastIndex -> Location.LAST
            else -> Location.MIDDLE
        }

        result += ReaderTextItem(
            id = "item_${id}_${orderCounter}",
            chapterId = id,
            order = orderCounter++,
            text = chunk,
            type = TextType.BODY,
            location = location
        )
    }

    return result
}

/** Chunk 1 đoạn dài thành nhiều đoạn <= maxLen, ưu tiên cắt tại xuống dòng hoặc dấu câu. */
private fun chunkByLength(text: String, maxLen: Int): List<String> {
    if (text.length <= maxLen) return listOf(text)

    val lines = text.split('\n')
    val out = mutableListOf<StringBuilder>()
    var cur = StringBuilder()

    fun flush() {
        if (cur.isNotEmpty()) {
            out += cur
            cur = StringBuilder()
        }
    }

    lines.forEachIndexed { i, line ->
        val add = if (cur.isEmpty()) line else "\n$line"
        if (cur.length + add.length <= maxLen) {
            cur.append(add)
        } else {
            // nếu dòng hiện tại quá dài, cắt thông minh theo câu/khoảng trắng
            if (add.length > maxLen) {
                // đẩy phần hiện tại
                flush()
                splitSmart(add.trimStart(), maxLen).forEach { piece ->
                    if (piece.length > maxLen) {
                        // fallback: hard split
                        piece.chunked(maxLen).forEach { hard ->
                            out += StringBuilder(hard)
                        }
                    } else {
                        out += StringBuilder(piece)
                    }
                }
                cur = StringBuilder()
            } else {
                // đẩy cur và bắt đầu block mới
                flush()
                cur.append(line)
            }
        }

        if (i == lines.lastIndex) flush()
    }

    return out.map { it.toString().trim() }.filter { it.isNotEmpty() }
}

/** Cắt theo dấu câu/khoảng trắng để giữ ngắt câu tự nhiên, từng đoạn <= maxLen. */
private fun splitSmart(text: String, maxLen: Int): List<String> {
    if (text.length <= maxLen) return listOf(text)

    val pieces = mutableListOf<String>()
    var start = 0

    while (start < text.length) {
        var end = (start + maxLen).coerceAtMost(text.length)
        if (end < text.length) {
            // cố gắng lùi đến dấu câu hoặc khoảng trắng gần nhất
            val slice = text.substring(start, end)
            val lastPunct = slice.lastIndexOfAny(charArrayOf('.', '!', '?', '…', '”', '’', ',', ';', ':', ' ', '\n'))
            if (lastPunct != -1 && lastPunct >= maxLen * 2 / 3) {
                end = start + lastPunct + 1
            }
        }
        pieces += text.substring(start, end).trim()
        start = end
    }

    return pieces.filter { it.isNotEmpty() }
}

private fun String.normalizeLineBreaks(): String =
    this.replace("\r\n", "\n").replace('\r', '\n')

private fun String?.orIfBlank(fallback: String): String =
    if (this.isNullOrBlank()) fallback else this