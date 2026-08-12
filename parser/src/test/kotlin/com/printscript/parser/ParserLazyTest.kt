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
        var count = 0
        val tokens = createLazyStream { count++ }

        val iterator = Parser(tokens, Version.V1_0).parse()
        assertTrue(iterator.hasNext())
        org.junit.jupiter.api.Assertions
            .assertNotNull(iterator.next() as PrintStatement)
        assertTrue(count <= 10, "Consumed count ($count) exceeds limit")
        assertTrue(iterator.hasNext())
        iterator.next()
        assertTrue(count <= 15, "Consumed count ($count) exceeds limit")
    }

    private fun createLazyStream(onConsume: () -> Unit): Iterator<Token> =
        sequence {
            for (i in 1..100) {
                yield(Token(TokenType.PRINTLN, "println", defaultSpan))
                yield(Token(TokenType.LPAREN, "(", defaultSpan))
                yield(Token(TokenType.NUMBER_LITERAL, "$i", defaultSpan))
                yield(Token(TokenType.RPAREN, ")", defaultSpan))
                yield(Token(TokenType.SEMICOLON, ";", defaultSpan))
            }
        }.map {
            onConsume()
            it
        }.iterator()
}
