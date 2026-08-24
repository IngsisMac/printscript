package com.printscript.cli.util

internal object CommandLineTokenizer {
    fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var quote: Char? = null

        for (c in input) {
            quote = processChar(c, quote, current, tokens)
        }
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }
        return tokens
    }

    private fun processChar(
        c: Char,
        quote: Char?,
        current: StringBuilder,
        tokens: MutableList<String>,
    ): Char? =
        when {
            quote == null && (c == '"' || c == '\'') -> c
            quote != null && c == quote -> null
            quote == null && c.isWhitespace() -> {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.setLength(0)
                }
                null
            }
            else -> {
                current.append(c)
                quote
            }
        }
}
