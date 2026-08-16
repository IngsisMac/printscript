package com.printscript.runner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.StringReader

class ConfigLoaderTest {
    @Test
    fun testParseSimpleJsonMap() {
        val json = """
            {
                "enforce-spacing-around-equals": true,
                "line-breaks-after-println": 2,
                "identifier-format": "camelCase"
            }
        """.trimIndent()

        val map = ConfigLoader.parseJsonToMap(json)

        assertEquals(true, map["enforce-spacing-around-equals"])
        assertEquals(2, map["line-breaks-after-println"])
        assertEquals("camelCase", map["identifier-format"])
    }

    @Test
    fun testParseJsonWithCommasInStrings() {
        val json = """
            {
                "message": "hello, world",
                "enabled": false,
                "escaped": "line1\nline2\t\"quoted\"",
                "nullVal": null
            }
        """.trimIndent()

        val map = ConfigLoader.parseJsonToMap(json)

        assertEquals("hello, world", map["message"])
        assertEquals(false, map["enabled"])
        assertEquals("line1\nline2\t\"quoted\"", map["escaped"])
        assertNull(map["nullVal"])
    }

    @Test
    fun testParseArraysAndNumbers() {
        val json = """
            {
                "numbers": [-1, 2.5, 0],
                "items": ["a", "b"],
                "emptyArr": [],
                "emptyObj": {}
            }
        """.trimIndent()

        val map = ConfigLoader.parseJsonToMap(json)

        @Suppress("UNCHECKED_CAST")
        val numbers = map["numbers"] as List<Any?>
        assertEquals(-1, numbers[0])
        assertEquals(2.5, numbers[1])
        assertEquals(0, numbers[2])

        @Suppress("UNCHECKED_CAST")
        val items = map["items"] as List<Any?>
        assertEquals(2, items.size)

        @Suppress("UNCHECKED_CAST")
        val emptyArr = map["emptyArr"] as List<Any?>
        assertTrue(emptyArr.isEmpty())

        @Suppress("UNCHECKED_CAST")
        val emptyObj = map["emptyObj"] as Map<String, Any?>
        assertTrue(emptyObj.isEmpty())
    }

    @Test
    fun testParseFromReader() {
        val json = """{"key": 'single_quoted', "count": 10}"""
        val reader = StringReader(json)

        val map = ConfigLoader.parseJsonToMap(reader)

        assertEquals("single_quoted", map["key"])
        assertEquals(10, map["count"])
    }

    @Test
    fun testEmptyJsonAndWhitespace() {
        assertTrue(ConfigLoader.parseJsonToMap("").isEmpty())
        assertTrue(ConfigLoader.parseJsonToMap("   ").isEmpty())
    }

    @Test
    fun testUnquotedKeysAndValues() {
        val json = "{ key1: unquotedVal, key2: true, key3: 123 }"
        val map = ConfigLoader.parseJsonToMap(json)

        assertEquals("unquotedVal", map["key1"])
        assertEquals(true, map["key2"])
        assertEquals(123, map["key3"])
    }

    @Test
    fun testRunnerConfigReaderOverloads() {
        val source = StringReader("let x: number = 5;")
        val config = StringReader("""{"enforce-spacing-around-equals": true}""")
        val writer = java.io.StringWriter()

        val formatResult = PrintScriptRunner.format(source, com.printscript.common.Version.V1_0, config, writer)
        assertTrue(formatResult.errors.isEmpty())
        assertFalse(writer.toString().isEmpty())

        val analyzeSource = StringReader("let x: number = 5;")
        val analyzeConfig = StringReader("""{"identifier-format": "camelCase"}""")
        val analyzeResult = PrintScriptRunner.analyze(analyzeSource, com.printscript.common.Version.V1_0, analyzeConfig)
        assertTrue(analyzeResult.errors.isEmpty())
    }
}
