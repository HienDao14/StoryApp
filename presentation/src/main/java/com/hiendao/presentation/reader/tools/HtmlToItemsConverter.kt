package com.hiendao.presentation.reader.tools

import com.hiendao.coreui.utils.BookTextMapper
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.reader.domain.ImgEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

internal fun htmlToItemsConverter(
    chapterUrl: String,
    chapterIndex: Int,
    chapterItemPositionDisplacement: Int,
    text: String
): List<ReaderItem> {
    val doc = Jsoup.parse(text)
    val body = doc.body()

    val items = mutableListOf<ReaderItem>()
    var currentItemPosition = chapterItemPositionDisplacement
    
    val sb = StringBuilder()
    
    fun flushText(force: Boolean = false) {
        val content = sb.toString()
        if (content.isNotBlank() || force) {
            // Only add if there's actual content or forced
            if (content.isNotEmpty()) {
                items.add(ReaderItem.Body(
                    chapterUrl = chapterUrl,
                    chapterIndex = chapterIndex,
                    chapterItemPosition = currentItemPosition++,
                    text = content,
                    location = ReaderItem.Location.MIDDLE, // Fixed later
                    isHtml = true
                ))
            }
            sb.clear()
        }
    }

    fun traverse(node: Node) {
        when (node) {
            is TextNode -> {
                 sb.append(node.text())
            }
            is Element -> {
                val tagName = node.tagName()
                if (tagName == "img") {
                    flushText(force = false)
                    val src = node.attr("src")
                    if (src.isNotEmpty()) {
                        val width = node.attr("width").toFloatOrNull()
                        val height = node.attr("height").toFloatOrNull()
                        val yrelAttr = node.attr("yrel").toFloatOrNull()
                        
                        // Default to -1f if cannot determine. Adapter should handle this or default to square/auto.
                        // If adapter expects strict float, we must provide one.
                        // Assuming -1f acts as a flag for "auto" in modified adapter.
                        val finalYrel = yrelAttr ?: if (width != null && height != null && width > 0) {
                            height / width
                        } else {
                            -1f 
                        }
                        
                        items.add(ReaderItem.Image(
                            chapterUrl = chapterUrl,
                            chapterIndex = chapterIndex,
                            chapterItemPosition = currentItemPosition++,
                            text = "",
                            location = ReaderItem.Location.MIDDLE,
                            image = ImgEntry(src, finalYrel)
                        ))
                    }
                } else if (tagName == "br") {
                    // BR is a natural breakpoint for TTS/Reader if deeper splitting is desired
                    // But if we just append <br>, it stays in the text.
                    // To split, we flush.
                    // However, we might want to check if the previous content is substantial?
                    // For now, simple split on BR.
                    flushText() 
                } else if (tagName == "p" || tagName == "div" || tagName == "section" || tagName == "article" || tagName == "blockquote") {
                    // Block elements: traverse if they contain images OR explicit breaks
                    val hasImages = !node.select("img").isEmpty()
                    val hasBreaks = !node.select("br").isEmpty()
                    
                    if (hasImages || hasBreaks) {
                        // Contains images or breaks, must split/recurse
                        for (child in node.childNodes()) {
                            traverse(child)
                        }
                        flushText()
                    } else {
                        // Safe to keep as block
                         if (sb.isNotEmpty()) flushText()
                        sb.append(node.outerHtml())
                        flushText() 
                    }
                } else {
                    // Inline or other elements
                     if (node.select("img").isEmpty()) {
                        sb.append(node.outerHtml())
                    } else {
                        // Recurse
                         for (child in node.childNodes()) {
                            traverse(child)
                        }
                    }
                }
            }
        }
    }
    
    // Start traversal
    for (child in body.childNodes()) {
        traverse(child)
    }
    flushText()
    
    // Fix locations
    if (items.isNotEmpty()) {
        val first = items.first()
        items[0] = copyWithLocation(first, ReaderItem.Location.FIRST)
        
        val last = items.last()
        items[items.lastIndex] = copyWithLocation(last, ReaderItem.Location.LAST)
    }

    return items
}

private fun copyWithLocation(item: ReaderItem, newLocation: ReaderItem.Location): ReaderItem {
    return when (item) {
        is ReaderItem.Body -> item.copy(location = newLocation)
        is ReaderItem.Image -> item.copy(location = newLocation)
        else -> item
    }
}
