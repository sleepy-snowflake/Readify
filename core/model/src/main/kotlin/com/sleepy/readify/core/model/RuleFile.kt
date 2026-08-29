package com.sleepy.readify.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RuleFile(
    @SerialName("schema") val schema: String,
    val id: String,
    val name: String,
    val version: Int,
    val domains: List<String>,
    val requiresJs: Boolean = false,
    val cleanup: Cleanup? = null,
    val listing: ListingSection? = null,
    val search: SearchSection? = null,
    val fields: RuleFields? = null,
    val content: ContentSection,
    val pagination: Pagination? = null,
    val js: JsSection? = null,
) {
    companion object {
        const val SCHEMA_V1 = "readify-rule/1"
    }
}

@Serializable
data class Cleanup(
    val remove: List<String> = emptyList(),
)

@Serializable
data class ListingSection(
    val item: String,
    val title: String,
    val link: String,
    val date: String? = null,
    val excerpt: String? = null,
    val nextPage: String? = null,
)

@Serializable
data class SearchSection(
    val url: String,
    val item: String,
    val title: String,
    val link: String,
    val date: String? = null,
    val excerpt: String? = null,
)

@Serializable
data class FieldSelector(
    val sel: String,
    val attr: String? = null,
)

@Serializable
data class RuleFields(
    val title: FieldSelector,
    val author: FieldSelector? = null,
    val date: FieldSelector? = null,
)

@Serializable
data class ContentSection(
    val container: String,
    val blocks: Map<String, BlockRule>,
)

@Serializable
data class BlockRule(
    val type: String,
    val level: Int? = null,
    val src: String? = null,
    val alt: String? = null,
    val caption: String? = null,
    val lang: String? = null,
    val ordered: Boolean? = null,
)

@Serializable
data class Pagination(
    val nextPage: String,
)

@Serializable
data class JsSection(
    val preExtract: String,
)
