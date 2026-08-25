package com.printscript.formatter.expression

import com.printscript.ast.Expression
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

interface ExpressionRule {
    fun appliesTo(expression: Expression): Boolean

    fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String
}
