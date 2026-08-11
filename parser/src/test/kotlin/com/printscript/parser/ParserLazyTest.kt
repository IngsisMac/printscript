package com.printscript.parser

import com.printscript.ast.PrintStatement
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParserLazyTest {
    private val defaultSpan = Span(Position(1, 1), Position(1, 10))

    // PS-PAR-012 & PS-PAR-013 — Lazy evaluation and bounded token lookahead
    @Test
    fun `parser consumes tokens lazily statement by statement`() {
        var consumedCount = 0

        // Create custom iterator that counts consumed tokens
        val tokenStream =
            sequence {
                for (i in 1..100) {
                    yield(Token(TokenType.PRINTLN, "println", defaultSpan))
                    yield(Token(TokenType.LPAREN, "(", defaultSpan))
                    yield(Token(TokenType.NUMBER_LITERAL, "$i", defaultSpan))
                    yield(Token(TokenType.RPAREN, ")", defaultSpan))
                    yield(Token(TokenType.SEMICOLON, ";", defaultSpan))
                }
            }.map { token ->
                consumedCount++
                token
            }.iterator()

        val parser = Parser(tokenStream, Version.V1_0)
        val statementIterator = parser.parse()

        assertTrue(statementIterator.hasNext())

        val firstStatement = statementIterator.next() as PrintStatement
        assertNotNull(firstStatement)

        // For the first statement (5 tokens), due to lookahead buffer of 2 in TokenStream,
        // consumedCount should be bounded and far less than all 500 tokens!
        assertTrue(consumedCount <= 10, "Consumed token count ($consumedCount) exceeds lookahead limit")

        // Parse second statement
        assertTrue(statementIterator.hasNext())
        statementIterator.next()
        assertTrue(consumedCount <= 15, "Consumed token count ($consumedCount) exceeds lookahead limit for 2 statements")
    }

    private fun assertNotNull(obj: Any?) {
        org.junit.jupiter.api.Assertions
            .assertNotNull(obj)
    }
}
