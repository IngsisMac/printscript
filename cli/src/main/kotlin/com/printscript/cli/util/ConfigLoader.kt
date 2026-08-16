package com.printscript.cli.util

import java.io.File

object ConfigLoader {
    fun loadConfig(file: File?): Map<String, Any?> {
        if (file == null || !file.exists()) return emptyMap()
        val content = file.readText().trim()
        if (content.isEmpty()) return emptyMap()
        return parseJsonToMap(content)
    }

    fun parseJsonToMap(json: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val cleaned =
            json
                .trim()
                .removePrefix("{")
                .removeSuffix("}")
                .trim()
        if (cleaned.isEmpty()) return result

        val entries = cleaned.split(",")
        for (entry in entries) {
            val pair = parseEntry(entry)
            if (pair != null) {
                result[pair.first] = pair.second
            }
        }
        return result
    }

    private fun parseEntry(entry: String): Pair<String, Any?>? {
        val parts = entry.split(":", limit = 2)
        if (parts.size != 2) return null
        val key = parts[0].trim().removeSurrounding("\"").removeSurrounding("'")
        val rawValue = parts[1].trim()
        return Pair(key, parseValue(rawValue))
    }

    private fun parseValue(rawValue: String): Any? =
        when {
            rawValue == "true" -> true
            rawValue == "false" -> false
            rawValue == "null" -> null
            rawValue.startsWith("\"") && rawValue.endsWith("\"") -> rawValue.removeSurrounding("\"")
            rawValue.startsWith("'") && rawValue.endsWith("'") -> rawValue.removeSurrounding("'")
            rawValue.toIntOrNull() != null -> rawValue.toInt()
            rawValue.toDoubleOrNull() != null -> rawValue.toDouble()
            else -> rawValue
        }
}
