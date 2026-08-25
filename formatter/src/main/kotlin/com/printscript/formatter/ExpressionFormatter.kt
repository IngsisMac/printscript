package com.printscript.formatter

import com.printscript.ast.Expression

interface ExpressionFormatter {
    fun format(
        expression: Expression,
        config: FormatterConfig,
    ): String

    companion object : ExpressionFormatter {
        private val defaultInstance = DefaultExpressionFormatter()

        override fun format(
            expression: Expression,
            config: FormatterConfig,
        ): String = defaultInstance.format(expression, config)
    }
}
