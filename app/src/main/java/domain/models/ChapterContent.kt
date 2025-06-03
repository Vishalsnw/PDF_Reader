/*
 * File: ChapterContent.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:18:17 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.domain.models

import java.util.regex.Pattern

data class ChapterContent(
    val chapterId: String,
    val bookId: String,
    val content: String,
    val contentType: ContentType = ContentType.HTML,
    val images: List<ContentImage> = emptyList(),
    val footnotes: Map<String, String> = emptyMap(),
    val annotations: List<ContentAnnotation> = emptyList(),
    val metadata: ContentMetadata = ContentMetadata(),
    val version: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        private const val WORDS_PER_MINUTE = 200
        private val WORD_PATTERN = Pattern.compile("\\s+")
        private const val MIN_READING_TIME = 1 // minutes
    }

    fun isEmpty(): Boolean = content.isEmpty() && images.isEmpty()

    fun hasImages(): Boolean = images.isNotEmpty()

    fun hasFootnotes(): Boolean = footnotes.isNotEmpty()

    fun hasAnnotations(): Boolean = annotations.isNotEmpty()

    fun estimateReadingTime(): Int {
        val wordCount = WORD_PATTERN.split(content).size
        return (wordCount / WORDS_PER_MINUTE).coerceAtLeast(MIN_READING_TIME)
    }

    fun getImageCount(): Int = images.size

    fun getTotalImageSize(): Long = images.sumOf { it.fileSize ?: 0L }

    fun findAnnotationsForRange(start: Int, end: Int): List<ContentAnnotation> {
        return annotations.filter { it.start in start..end || it.end in start..end }
    }

    fun getContentWithoutMarkup(): String {
        return when (contentType) {
            ContentType.HTML -> removeHtmlTags(content)
            ContentType.MARKDOWN -> removeMarkdownFormatting(content)
            ContentType.PLAIN -> content
        }
    }

    private fun removeHtmlTags(html: String): String {
        return html.replace(HTML_TAG_PATTERN, "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun removeMarkdownFormatting(markdown: String): String {
        return markdown.replace(MARKDOWN_PATTERN, "")
            .replace("\\[.*?\\]\\(.*?\\)".toRegex(), "") // Remove links
            .replace("#{1,6}\\s".toRegex(), "") // Remove headers
            .trim()
    }
}

data class ContentImage(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val caption: String? = null,
    val altText: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val placeholderUrl: String? = null
) {
    fun aspectRatio(): Float = width.toFloat() / height.toFloat()
    
    fun isLocallyAvailable(): Boolean = isDownloaded && !localPath.isNullOrBlank()
    
    fun getDisplayUrl(): String = localPath ?: placeholderUrl ?: url
    
    fun requiresDownload(): Boolean = !isDownloaded && !url.isNullOrBlank()
}

data class ContentAnnotation(
    val id: String,
    val type: AnnotationType,
    val start: Int,
    val end: Int,
    val content: String,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

data class ContentMetadata(
    val wordCount: Int? = null,
    val readingLevel: String? = null,
    val language: String = "en",
    val translator: String? = null,
    val revision: Int = 1,
    val tags: List<String> = emptyList(),
    val source: String? = null
)

enum class ContentType {
    HTML,
    MARKDOWN,
    PLAIN
}

enum class AnnotationType {
    HIGHLIGHT,
    NOTE,
    BOOKMARK,
    COMMENT,
    DEFINITION,
    TRANSLATION
}

/**
 * Extension functions for content processing
 */
fun ChapterContent.extractTextContent(): String {
    return getContentWithoutMarkup()
}

fun ChapterContent.getImageUrls(): List<String> {
    return images.map { it.url }
}

fun ChapterContent.findFootnoteReferences(): Set<String> {
    return footnotes.keys.toSet()
}

/**
 * Utility functions for content manipulation
 */
object ContentUtils {
    private val HTML_TAG_PATTERN = "<[^>]*>".toRegex()
    private val MARKDOWN_PATTERN = "([*_~`]){1,3}.*?\\1{1,3}".toRegex()

    fun sanitizeContent(content: String, contentType: ContentType): String {
        return when (contentType) {
            ContentType.HTML -> sanitizeHtml(content)
            ContentType.MARKDOWN -> sanitizeMarkdown(content)
            ContentType.PLAIN -> sanitizePlainText(content)
        }
    }

    private fun sanitizeHtml(html: String): String {
        return html.replace("<script.*?</script>".toRegex(RegexOption.DOT_MATCHES_ALL), "")
            .replace("<style.*?</style>".toRegex(RegexOption.DOT_MATCHES_ALL), "")
            .replace("javascript:", "")
            .trim()
    }

    private fun sanitizeMarkdown(markdown: String): String {
        return markdown.replace("[^\\x20-\\x7E\n]".toRegex(), "")
            .trim()
    }

    private fun sanitizePlainText(text: String): String {
        return text.replace("[^\\x20-\\x7E\n]".toRegex(), "")
            .trim()
    }
}

private val HTML_TAG_PATTERN = "<[^>]*>".toRegex()
private val MARKDOWN_PATTERN = "([*_~`]){1,3}.*?\\1{1,3}".toRegex()
