package com.printscript.lexer

import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.lexer.matchers.IdentifierMatcher
import com.printscript.lexer.matchers.NumberMatcher
import com.printscript.lexer.matchers.StringMatcher
import com.printscript.lexer.matchers.SymbolMatcher
import com.printscript.lexer.matchers.TokenMatcher
import com.printscript.token.Token
import java.io.Reader

class Lexer(
    source: Reader,
    private val version: Version,
    private val config: TokenConfig = TokenConfig.from(version),
    private val matchers: List<TokenMatcher> =
        listOf(
            SymbolMatcher(),
            StringMatcher(),
            NumberMatcher(),
            IdentifierMatcher(),
        ),
) : Iterator<Token> {
    private val stream = CharStream(source)
    private var nextToken: Token? = null

    override fun hasNext(): Boolean {
        if (nextToken != null) return true
        if (!stream.hasMore()) return false
        nextToken = readNextToken()
        return nextToken != null
    }

    override fun next(): Token {
        if (hasNext()) {
            val token = nextToken!!
            nextToken = null
            return token
        }
        throw NoSuchElementException("No more tokens")
    }

    private fun readNextToken(): Token? {
        stream.skipWhitespace()

        if (!stream.hasMore()) return null

        for (matcher in matchers) {
            if (matcher.canMatch(stream)) {
                return matcher.match(stream, config)
            }
        }

        val startPos = stream.getPosition()
        val badChar = stream.peek()!!
        stream.advance()
        val endPos = stream.getPosition()
        throw LexerException("Unexpected character '$badChar'", Span(startPos, endPos))
    }
}
