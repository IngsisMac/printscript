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

    private class JsonParser(private val json: String) {
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
                else -> {
                    if (c == '-' || c.isDigit()) {
                        parseNumber()
                    } else {
                        parseUnquotedString()
                    }
                }
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            skipWhitespace()
            val map = mutableMapOf<String, Any?>()
            if (peek() == '}') {
                index++
                return map
            }
            while (index < json.length) {
                skipWhitespace()
                val key = parseStringOrUnquoted()
                skipWhitespace()
                expect(':')
                skipWhitespace()
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (peek() == ',') {
                    index++
                } else if (peek() == '}') {
                    index++
                    break
                } else {
                    break
                }
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            skipWhitespace()
            val list = mutableListOf<Any?>()
            if (peek() == ']') {
                index++
                return list
            }
            while (index < json.length) {
                skipWhitespace()
                list.add(parseValue())
                skipWhitespace()
                if (peek() == ',') {
                    index++
                } else if (peek() == ']') {
                    index++
                    break
                } else {
                    break
                }
            }
            return list
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
            var escaped = false
            while (index < json.length) {
                val c = json[index++]
                if (escaped) {
                    when (c) {
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        else -> sb.append(c)
                    }
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == quote) {
                    return sb.toString()
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseUnquotedString(): String {
            val sb = StringBuilder()
            while (index < json.length) {
                val c = json[index]
                if (c.isWhitespace() || c == ':' || c == ',' || c == '}' || c == ']') break
                sb.append(c)
                index++
            }
            return sb.toString()
        }

        private fun parseBoolean(): Boolean {
            return if (json.startsWith("true", index)) {
                index += 4
                true
            } else if (json.startsWith("false", index)) {
                index += 5
                false
            } else {
                parseUnquotedString().toBoolean()
            }
        }

        private fun parseNull(): Any? {
            return if (json.startsWith("null", index)) {
                index += 4
                null
            } else {
                parseUnquotedString()
            }
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
