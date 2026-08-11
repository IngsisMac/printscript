package com.printscript.lexer

import com.printscript.common.PrintScriptError
import com.printscript.common.Span

class LexerException(
    override val message: String,
    val span: Span,
) : RuntimeException(message) {
    fun toError(): PrintScriptError = PrintScriptError(message, span)
}
