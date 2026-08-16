package com.printscript.cli.util

import java.io.File

object ConfigLoader {
    fun loadConfig(file: File?): Map<String, Any?> {
        if (file == null || !file.exists()) return emptyMap()
        val content = file.readText().trim()
        if (content.isEmpty()) return emptyMap()
        return com.printscript.runner.ConfigLoader.parseJsonToMap(content)
    }

    fun parseJsonToMap(json: String): Map<String, Any?> =
        com.printscript.runner.ConfigLoader.parseJsonToMap(json)
}
