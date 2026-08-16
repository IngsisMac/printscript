package com.printscript.runner

import java.io.Reader

object ConfigLoader {
    fun parseJsonToMap(reader: Reader): Map<String, Any?> {
        val content = reader.readText()
        return parseJsonToMap(content)
    }

    fun parseJsonToMap(json: String): Map<String, Any?> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyMap()
        val parser = JsonParser(trimmed)
        val value = parser.parseValue()
        @Suppress("UNCHECKED_CAST")
        return (value as? Map<String, Any?>) ?: emptyMap()
    }

    private class JsonParser(
        private val json: String,
    ) {
        private var index = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (index >= json.length) return null
            return when (val c = json[index]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"', '\'' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> parseNumberOrUnquoted(c)
            }
        }

        private fun parseNumberOrUnquoted(c: Char): Any? =
            if (c == '-' || c.isDigit()) {
                parseNumber()
            } else {
                parseUnquotedString()
            }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            skipWhitespace()
            val map = mutableMapOf<String, Any?>()
            if (peek() == '}') {
                index++
                return map
            }
            populateObject(map)
            return map
        }

        private fun populateObject(map: MutableMap<String, Any?>) {
            while (index < json.length) {
                skipWhitespace()
                val key = parseStringOrUnquoted()
                skipWhitespace()
                expect(':')
                skipWhitespace()
                map[key] = parseValue()
                skipWhitespace()
                if (!advanceAfterDelimiter('}')) break
            }
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            skipWhitespace()
            val list = mutableListOf<Any?>()
            if (peek() == ']') {
                index++
                return list
            }
            populateArray(list)
            return list
        }

        private fun populateArray(list: MutableList<Any?>) {
            while (index < json.length) {
                skipWhitespace()
                list.add(parseValue())
                skipWhitespace()
                if (!advanceAfterDelimiter(']')) break
            }
        }

        private fun advanceAfterDelimiter(closingChar: Char): Boolean {
            val p = peek()
            if (p == ',') {
                index++
                return true
            }
            if (p == closingChar) {
                index++
                return false
            }
            return false
        }

        private fun parseStringOrUnquoted(): String {
            skipWhitespace()
            return if (peek() == '"' || peek() == '\'') {
                parseString()
            } else {
                parseUnquotedString()
            }
        }

        private fun parseString(): String {
            val quote = json[index++]
            val sb = StringBuilder()
            while (index < json.length) {
                val c = json[index++]
                if (c == quote) return sb.toString()
                if (c == '\\' && index < json.length) {
                    sb.append(readEscapedChar())
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun readEscapedChar(): Char =
            when (val c = json[index++]) {
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                else -> c
            }

        private fun parseUnquotedString(): String {
            val sb = StringBuilder()
            while (index < json.length) {
                val c = json[index]
                if (isDelimiter(c)) break
                sb.append(c)
                index++
            }
            return sb.toString()
        }

        private fun isDelimiter(c: Char): Boolean = c.isWhitespace() || c in ":,}]"

        private fun parseBoolean(): Boolean =
            if (json.startsWith("true", index)) {
                index += 4
                true
            } else if (json.startsWith("false", index)) {
                index += 5
                false
            } else {
                parseUnquotedString().toBoolean()
            }

        private fun parseNull(): Any? =
            if (json.startsWith("null", index)) {
                index += 4
                null
            } else {
                parseUnquotedString()
            }

        private fun parseNumber(): Number {
            val start = index
            if (json[index] == '-') index++
            while (index < json.length && json[index].isDigit()) index++
            if (index < json.length && json[index] == '.') {
                index++
                while (index < json.length && json[index].isDigit()) index++
                val str = json.substring(start, index)
                return str.toDoubleOrNull() ?: 0.0
            }
            val str = json.substring(start, index)
            return str.toIntOrNull() ?: 0
        }

        private fun skipWhitespace() {
            while (index < json.length && json[index].isWhitespace()) {
                index++
            }
        }

        private fun peek(): Char = if (index < json.length) json[index] else '\u0000'

        private fun expect(expected: Char) {
            skipWhitespace()
            if (index < json.length && json[index] == expected) {
                index++
            }
        }
    }
}
