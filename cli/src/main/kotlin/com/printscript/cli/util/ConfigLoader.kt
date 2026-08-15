package com.printscript.cli.util

import java.io.File

object ConfigLoader {
    fun loadConfig(file: File?): Map<String, Any?> {
        if (file == null || !file.exists()) {
            return emptyMap()
        }
        val content = file.readText().trim()
        if (content.isEmpty()) {
            return emptyMap()
        }
        return parseJsonToMap(content)
    }

    fun parseJsonToMap(json: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val cleaned = json.trim().removePrefix("{").removeSuffix("}").trim()
        if (cleaned.isEmpty()) {
            return result
        }

        // Simple regex/line splitter for flat JSON config keys: "key": value
        val entries = cleaned.split(",")
        for (entry in entries) {
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().removeSurrounding("\"").removeSurrounding("'")
                val rawValue = parts[1].trim()
                val parsedValue: Any? = when {
                    rawValue == "true" -> true
                    rawValue == "false" -> false
                    rawValue == "null" -> null
                    rawValue.startsWith("\"") && rawValue.endsWith("\"") -> rawValue.removeSurrounding("\"")
                    rawValue.startsWith("'") && rawValue.endsWith("'") -> rawValue.removeSurrounding("'")
                    rawValue.toIntOrNull() != null -> rawValue.toInt()
                    rawValue.toDoubleOrNull() != null -> rawValue.toDouble()
                    else -> rawValue
                }
                result[key] = parsedValue
            }
        }
        return result
    }
}
