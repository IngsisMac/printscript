package com.printscript.formatter

data class FormatterContext(
    val config: FormatterConfig = FormatterConfig(),
    val indentLevel: Int = 0,
    val formatter: Formatter,
    val expressionFormatter: ExpressionFormatter,
) {
    fun nextIndent(): FormatterContext = copy(indentLevel = indentLevel + 1)

    val indentString: String
        get() = " ".repeat(indentLevel * config.indentInsideIf)
}
