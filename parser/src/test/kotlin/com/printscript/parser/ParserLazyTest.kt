package com.printscript.parser

import com.printscript.ast.PrintStatement
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenFactory
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ParserLazyTest {
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        version = Version.V1_0
    }

    @Test
    @DisplayName("Parser consume tokens de forma perezosa sentencia por sentencia")
    fun parserConsumesTokensLazilyStatementByStatement() {
        var count = 0
        val tokens = createLazyStream { count++ }

        val iterator = Parser(tokens, version).parse()

        assertTrue(iterator.hasNext())
        assertNotNull(iterator.next() as PrintStatement)
        assertTrue(count <= 10, "Consumed count ($count) exceeds limit")

        assertTrue(iterator.hasNext())
        iterator.next()

        assertTrue(count <= 15, "Consumed count ($count) exceeds limit")
    }

    private fun createLazyStream(onConsume: () -> Unit): Iterator<Token> =
        sequence {
            for (i in 1..100) {
                yieldAll(
                    TokenFactory.tokens(
                        TokenType.PRINTLN,
                        TokenType.LPAREN,
                        TokenType.NUMBER_LITERAL to "$i",
                        TokenType.RPAREN,
                        TokenType.SEMICOLON,
                    ),
                )
            }
        }.map {
            onConsume()
            it
        }.iterator()
}
