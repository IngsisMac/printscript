package com.printscript.lexer

import com.printscript.common.PrintScriptError
import com.printscript.common.PrintScriptFailure
import com.printscript.common.Span

class LexerException(
    override val message: String,
    override val span: Span,
) : RuntimeException(message),
    PrintScriptFailure {
    override val rawMessage: String
        get() = message

    fun toError(): PrintScriptError = PrintScriptError(message, span)
}
