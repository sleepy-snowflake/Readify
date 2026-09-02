package com.sleepy.readify.core.rules

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaConsistencyTest {

    @Test
    fun bundledSchemaMatchesAuthoritativeSchemaFile() {
        val bundled = javaClass.getResourceAsStream("/readify-rule-1.schema.json")!!
            .readBytes()
            .toList()
        val authoritative = File("schemas/readify-rule-1.schema.json")
            .readBytes()
            .toList()

        assertEquals(authoritative, bundled)
    }
}
