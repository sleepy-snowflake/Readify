package com.sleepy.readify.core.model

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleFileSerializationTest {

    private val json = Json

    @Test
    fun ruleFileRoundTrips() {
        val rule = RuleFile(
            schema = RuleFile.SCHEMA_V1,
            id = "example-blog",
            name = "Example Blog",
            version = 3,
            domains = listOf("example.com", "blog.example.com"),
            requiresJs = false,
            cleanup = Cleanup(remove = listOf("nav", ".ads", "script", "style")),
            listing = ListingSection(
                item = "article.post",
                title = "h2 a",
                link = "h2 a@href",
                date = "time@datetime?",
                excerpt = ".summary?",
                nextPage = "a.next?",
            ),
            search = SearchSection(
                url = "https://example.com/?s={query}",
                item = "article.post",
                title = "h2 a",
                link = "h2 a@href",
                date = "time@datetime?",
            ),
            fields = RuleFields(
                title = FieldSelector(sel = "h1.article-title"),
                author = FieldSelector(sel = ".byline .author?"),
                date = FieldSelector(sel = "time?", attr = "datetime"),
            ),
            content = ContentSection(
                container = "article.post",
                blocks = mapOf(
                    "h2" to BlockRule(type = "heading", level = 2),
                    "p" to BlockRule(type = "paragraph"),
                    "img" to BlockRule(type = "image", src = "src", alt = "alt"),
                    "pre" to BlockRule(type = "code", lang = "code@class?"),
                    "ul" to BlockRule(type = "list"),
                    "ol" to BlockRule(type = "list", ordered = true),
                    "table" to BlockRule(type = "table"),
                ),
            ),
            pagination = Pagination(nextPage = "a.next?"),
            js = JsSection(preExtract = "function(doc){ return doc; }"),
        )

        val encoded = json.encodeToString(rule)
        assertEquals(rule, json.decodeFromString<RuleFile>(encoded))
        assertTrue("\"schema\":\"readify-rule/1\"" in encoded)
    }

    @Test
    fun unknownKeysAreRejected() {
        val invalid = """
            {"schema":"readify-rule/1","id":"a","name":"A","version":1,
             "domains":["a.com"],
             "content":{"container":"main","blocks":{}},
             "registry":"https://example.com/rules.json"}
        """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<RuleFile>(invalid) }
    }
}
