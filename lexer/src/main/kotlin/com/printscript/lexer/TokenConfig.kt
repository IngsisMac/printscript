package com.printscript.lexer

import com.printscript.common.Version
import com.printscript.token.TokenType

class TokenConfig(
    val keywords: Map<String, TokenType>,
    val version: Version,
) {
    companion object {
        private val COMMON_KEYWORDS =
            mapOf(
                "let" to TokenType.LET,
                "number" to TokenType.NUMBER,
                "string" to TokenType.STRING,
                "println" to TokenType.PRINTLN,
            )

        private val V11_KEYWORDS =
            mapOf(
                "const" to TokenType.CONST,
                "boolean" to TokenType.BOOLEAN,
                "if" to TokenType.IF,
                "else" to TokenType.ELSE,
                "readInput" to TokenType.READ_INPUT,
                "readEnv" to TokenType.READ_ENV,
                "true" to TokenType.TRUE,
                "false" to TokenType.FALSE,
            )

        fun from(version: Version): TokenConfig {
            val keywords =
                when (version) {
                    Version.V1_0 -> COMMON_KEYWORDS
                    Version.V1_1 -> COMMON_KEYWORDS + V11_KEYWORDS
                }

            return TokenConfig(keywords, version)
        }
    }
}
