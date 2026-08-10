package com.printscript.common

data class PrintScriptError(
    val message: String,
    val span: Span,
) {
    fun render(): String = "$span Error: $message"

    override fun toString() = render()
}
