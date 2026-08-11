package com.printscript.lexer.matchers

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.lexer.CharStream
import com.printscript.lexer.LexerException
import com.printscript.lexer.TokenConfig
import com.printscript.token.Token
import com.printscript.token.TokenType

class StringMatcher : TokenMatcher {
    override fun canMatch(stream: CharStream): Boolean {
        val ch = stream.peek() ?: return false
        return ch == '"' || ch == '\''
    }

    override fun match(
        stream: CharStream,
        config: TokenConfig,
    ): Token {
        val startPos = stream.getPosition()
        val quoteChar = stream.advance()!!
        val sb = StringBuilder()
        var isClosed = false

        while (stream.hasMore()) {
            val ch = stream.peek()!!
            if (ch == quoteChar) {
                isClosed = true
                stream.advance()
                break
            }
            sb.append(ch)
            stream.advance()
        }

        if (!isClosed) {
            val errPos = stream.getPosition()
            throw LexerException("Unterminated string literal", Span(startPos, errPos))
        }

        val endPos = Position(stream.line, stream.column - 1)
        return Token(TokenType.STRING_LITERAL, sb.toString(), Span(startPos, endPos))
    }
}
