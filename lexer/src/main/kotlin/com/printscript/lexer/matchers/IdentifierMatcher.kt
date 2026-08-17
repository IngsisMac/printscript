package com.printscript.lexer.matchers

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.lexer.CharStream
import com.printscript.lexer.TokenConfig
import com.printscript.token.Token
import com.printscript.token.TokenType

class IdentifierMatcher : TokenMatcher {
    override fun canMatch(stream: CharStream): Boolean {
        val ch = stream.peek() ?: return false
        return ch.isLetter() || ch == '_'
    }

    override fun match(
        stream: CharStream,
        config: TokenConfig,
    ): Token {
        val startPos = stream.getPosition()
        val sb = StringBuilder()

        while (stream.hasMore()) {
            val ch = stream.peek()!!
            if (ch.isLetterOrDigit() || ch == '_') {
                sb.append(ch)
                stream.advance()
            } else {
                break
            }
        }

        val lexeme = sb.toString()
        val type = config.keywords[lexeme] ?: TokenType.IDENTIFIER
        val endPos = stream.getPreviousPosition()
        return Token(type, lexeme, Span(startPos, endPos))
    }
}
