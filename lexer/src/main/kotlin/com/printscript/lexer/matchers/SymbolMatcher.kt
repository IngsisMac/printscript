package com.printscript.lexer.matchers

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.lexer.CharStream
import com.printscript.lexer.TokenConfig
import com.printscript.token.Token
import com.printscript.token.TokenType

class SymbolMatcher : TokenMatcher {
    private val symbols =
        mapOf(
            '(' to TokenType.LPAREN,
            ')' to TokenType.RPAREN,
            '{' to TokenType.LBRACE,
            '}' to TokenType.RBRACE,
            ';' to TokenType.SEMICOLON,
            ',' to TokenType.COMMA,
            ':' to TokenType.COLON,
            '+' to TokenType.PLUS,
            '-' to TokenType.MINUS,
            '*' to TokenType.STAR,
            '/' to TokenType.SLASH,
            '=' to TokenType.EQUAL,
        )

    override fun canMatch(stream: CharStream): Boolean {
        val ch = stream.peek() ?: return false
        return symbols.containsKey(ch)
    }

    override fun match(
        stream: CharStream,
        config: TokenConfig,
    ): Token {
        val startPos = stream.getPosition()
        val ch = stream.advance()!!
        val type = symbols[ch]!!
        val endPos = Position(stream.line, stream.column - 1)
        return Token(type, ch.toString(), Span(startPos, endPos))
    }
}
