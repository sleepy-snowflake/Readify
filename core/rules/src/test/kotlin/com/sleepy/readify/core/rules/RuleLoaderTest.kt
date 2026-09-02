package com.sleepy.readify.core.rules

import com.sleepy.readify.core.model.DomainError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleLoaderTest {

    private val loader = RuleLoader.fromClasspath()

    private fun fixture(path: String): String =
        javaClass.getResourceAsStream(path)!!.readBytes().decodeToString()

    @Test
    fun loadsValidExampleRule() {
        val rule = loader.load(fixture("/rules/valid/example-blog.json"))

        assertEquals("example-blog", rule.id)
        assertEquals("readify-rule/1", rule.schema)
        assertEquals(3, rule.version)
        assertEquals(listOf("example.com", "blog.example.com"), rule.domains)
        assertEquals(10, rule.content.blocks.size)
        assertEquals("heading", rule.content.blocks["h2"]?.type)
    }

    @Test
    fun loadsMinimalRule() {
        val rule = loader.load(fixture("/rules/valid/minimal.json"))

        assertEquals("minimal-site", rule.id)
        assertEquals("paragraph", rule.content.blocks["p"]?.type)
    }

    @Test
    fun rejectsOversizedRule() {
        val padding = " ".repeat(RuleLoader.MAX_RULE_CHARS)

        val error = assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/valid/minimal.json") + padding)
        }
        assertTrue("limit" in error.message!!)
    }

    @Test
    fun rejectsMalformedJson() {
        val error = assertFailsWith<DomainError.RuleInvalid> { loader.load("{ not json") }
        assertTrue("not valid JSON" in error.message!!)
    }

    @Test
    fun rejectsMissingId() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/missing-id.json"))
        }
    }

    @Test
    fun rejectsWrongSchemaValue() {
        val error = assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/wrong-schema-value.json"))
        }
        assertTrue("schema" in error.message!!)
    }

    @Test
    fun rejectsNonIntegerVersion() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/version-not-integer.json"))
        }
    }

    @Test
    fun rejectsEmptyDomains() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/empty-domains.json"))
        }
    }

    @Test
    fun rejectsMissingContent() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/missing-content.json"))
        }
    }

    @Test
    fun rejectsUnknownBlockType() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/unknown-block-type.json"))
        }
    }

    @Test
    fun rejectsUnknownTopLevelKey() {
        assertFailsWith<DomainError.RuleInvalid> {
            loader.load(fixture("/rules/invalid/unknown-top-level-key.json"))
        }
    }
}
