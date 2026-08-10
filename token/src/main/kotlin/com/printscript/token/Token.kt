package com.printscript.token

import com.printscript.common.Span

data class Token(
    val type: TokenType,
    val lexeme: String,
    val span: Span,
)
