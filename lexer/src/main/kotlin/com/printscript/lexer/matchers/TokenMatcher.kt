package com.printscript.lexer.matchers

import com.printscript.lexer.CharStream
import com.printscript.lexer.TokenConfig
import com.printscript.token.Token

interface TokenMatcher {
    fun canMatch(stream: CharStream): Boolean

    fun match(
        stream: CharStream,
        config: TokenConfig,
    ): Token
}
