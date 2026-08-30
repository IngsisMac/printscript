package com.printscript.common

interface PrintScriptFailure {
    val rawMessage: String
    val span: Span
}
