package com.sleepy.readify.core.rules

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.sleepy.readify.core.model.DomainError
import com.sleepy.readify.core.model.RuleFile
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

class RuleLoader(schemaJson: String) {

    private val mapper = ObjectMapper()
    private val json = Json
    private val schema = JsonSchemaFactory
        .getInstance(SpecVersion.VersionFlag.V7)
        .getSchema(schemaJson)

    fun load(ruleJson: String): RuleFile {
        if (ruleJson.length > MAX_RULE_CHARS) {
            throw DomainError.RuleInvalid(
                "Rule file is ${ruleJson.length} characters; the limit is $MAX_RULE_CHARS",
            )
        }
        val document = try {
            mapper.readTree(ruleJson)
        } catch (e: IOException) {
            throw DomainError.RuleInvalid("Rule file is not valid JSON: ${e.message}")
        }
        val problems = schema.validate(document)
        if (problems.isNotEmpty()) {
            throw DomainError.RuleInvalid(
                "Rule file violates the readify-rule/1 schema:\n" +
                    problems.sortedBy { it.message }.joinToString("\n") { "- ${it.message}" },
            )
        }
        return try {
            json.decodeFromString<RuleFile>(ruleJson)
        } catch (e: SerializationException) {
            throw DomainError.RuleInvalid("Rule file failed contract mapping: ${e.message}")
        }
    }

    companion object {
        const val MAX_RULE_CHARS = 64 * 1024
        private const val SCHEMA_RESOURCE = "/readify-rule-1.schema.json"

        fun fromClasspath(): RuleLoader {
            val stream = RuleLoader::class.java.getResourceAsStream(SCHEMA_RESOURCE)
                ?: throw IllegalStateException("Bundled schema $SCHEMA_RESOURCE is missing")
            return stream.use { RuleLoader(it.readBytes().decodeToString()) }
        }
    }
}
