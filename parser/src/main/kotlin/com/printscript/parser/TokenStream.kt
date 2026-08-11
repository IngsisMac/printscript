package com.printscript.parser

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.token.Token
import com.printscript.token.TokenType

class TokenStream(
    private val tokens: Iterator<Token>,
) {
    private val buffer = java.util.ArrayDeque<Token>()

    fun peek(): Token {
        fillBuffer(1)
        return if (buffer.isNotEmpty()) buffer.first else eofToken()
    }

    fun peekNext(): Token {
        fillBuffer(2)
        return if (buffer.size >= 2) {
            val iterator = buffer.iterator()
            iterator.next()
            iterator.next()
        } else {
            eofToken()
        }
    }

    fun consume(): Token {
        fillBuffer(1)
        return if (buffer.isNotEmpty()) buffer.removeFirst() else eofToken()
    }

    fun check(type: TokenType): Boolean = peek().type == type

    fun checkAny(vararg types: TokenType): Boolean = peek().type in types

    fun match(type: TokenType): Boolean {
        if (check(type)) {
            consume()
            return true
        }
        return false
    }

    fun expect(vararg types: TokenType): Token {
        val token = peek()
        if (token.type !in types) {
            val expectedStr = if (types.size == 1) types[0].name else types.joinToString(" or ")
            throw ParseException(
                "Expected $expectedStr but got '${token.lexeme}' (${token.type})",
                token.span,
            )
        }
        return consume()
    }

    private fun fillBuffer(count: Int) {
        while (buffer.size < count && tokens.hasNext()) {
            buffer.addLast(tokens.next())
        }
    }

    private fun eofToken(): Token = Token(TokenType.EOF, "", Span(Position(1, 1), Position(1, 1)))
}
