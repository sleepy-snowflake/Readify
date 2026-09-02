package com.sleepy.readify.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InlineType {
    @SerialName("text") TEXT,
    @SerialName("link") LINK,
    @SerialName("em") EM,
    @SerialName("strong") STRONG,
    @SerialName("code") CODE,
}

@Serializable
data class InlineRun(
    @SerialName("t") val type: InlineType,
    @SerialName("s") val text: String,
    @SerialName("href") val href: String? = null,
)

@Serializable
data class ListItem(
    val runs: List<InlineRun> = emptyList(),
    val list: Block.ListBlock? = null,
)

@Serializable
data class TableCell(
    val runs: List<InlineRun> = emptyList(),
)

@Serializable
sealed interface Block {

    @Serializable
    @SerialName("heading")
    data class Heading(
        val level: Int = 1,
        val runs: List<InlineRun> = emptyList(),
    ) : Block

    @Serializable
    @SerialName("paragraph")
    data class Paragraph(
        val runs: List<InlineRun> = emptyList(),
    ) : Block

    @Serializable
    @SerialName("image")
    data class Image(
        val src: String,
        val alt: String? = null,
    ) : Block

    @Serializable
    @SerialName("figure")
    data class Figure(
        val src: String,
        val caption: String? = null,
    ) : Block

    @Serializable
    @SerialName("quote")
    data class Quote(
        val runs: List<InlineRun> = emptyList(),
        val cite: String? = null,
    ) : Block

    @Serializable
    @SerialName("code")
    data class Code(
        val text: String,
        val lang: String? = null,
    ) : Block

    @Serializable
    @SerialName("list")
    data class ListBlock(
        val ordered: Boolean = false,
        val items: List<ListItem> = emptyList(),
    ) : Block

    @Serializable
    @SerialName("table")
    data class Table(
        val rows: List<List<TableCell>> = emptyList(),
        val header: Boolean? = null,
    ) : Block

    @Serializable
    @SerialName("hr")
    data object Hr : Block
}

@Serializable
data class ArticleDocument(
    val title: String,
    val author: String? = null,
    val date: String? = null,
    val source: String? = null,
    val cover: String? = null,
    val blocks: List<Block> = emptyList(),
)
