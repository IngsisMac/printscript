package com.printscript.lexer

import com.printscript.common.Version
import com.printscript.token.TokenType

class TokenConfig(
    val keywords: Map<String, TokenType>,
    val version: Version,
) {
    companion object {
        fun from(version: Version): TokenConfig {
            val commonKeywords =
                mapOf(
                    "let" to TokenType.LET,
                    "number" to TokenType.NUMBER,
                    "string" to TokenType.STRING,
                    "println" to TokenType.PRINTLN,
                )

            val v11Keywords =
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

            val keywords =
                when (version) {
                    Version.V1_0 -> commonKeywords
                    Version.V1_1 -> commonKeywords + v11Keywords
                }

            return TokenConfig(keywords, version)
        }
    }
}
