package com.printscript.lexer.matchers

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.lexer.CharStream
import com.printscript.lexer.LexerException
import com.printscript.lexer.TokenConfig
import com.printscript.token.Token
import com.printscript.token.TokenType

class NumberMatcher : TokenMatcher {
    override fun canMatch(stream: CharStream): Boolean {
        val ch = stream.peek() ?: return false
        return ch.isDigit()
    }

    override fun match(
        stream: CharStream,
        config: TokenConfig,
    ): Token {
        val startPos = stream.getPosition()
        val sb = StringBuilder()
        var hasDecimalPoint = false

        while (stream.hasMore()) {
            val ch = stream.peek()!!
            if (ch.isDigit()) {
                sb.append(ch)
                stream.advance()
            } else if (ch == '.') {
                if (hasDecimalPoint) {
                    val errPos = stream.getPosition()
                    throw LexerException(
                        "Invalid number literal: multiple decimal points in '$sb.'",
                        Span(startPos, errPos),
                    )
                }
                hasDecimalPoint = true
                sb.append(ch)
                stream.advance()
            } else {
                break
            }
        }

        val lexeme = sb.toString()
        if (lexeme.endsWith(".")) {
            val endPos = Position(stream.line, stream.column - 1)
            throw LexerException(
                "Invalid number literal: trailing decimal point in '$lexeme'",
                Span(startPos, endPos),
            )
        }

        val endPos = Position(stream.line, stream.column - 1)
        return Token(TokenType.NUMBER_LITERAL, lexeme, Span(startPos, endPos))
    }
}
