package com.printscript.formatter.expression

import com.printscript.ast.BooleanLiteral
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

class LiteralExpressionRule : ExpressionRule {
    override fun appliesTo(expression: Expression): Boolean =
        expression is NumberLiteral || expression is StringLiteral || expression is BooleanLiteral

    override fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String =
        when (expression) {
            is NumberLiteral -> expression.value
            is StringLiteral -> "\"${expression.value}\""
            is BooleanLiteral -> expression.value.toString()
            else -> error("Unsupported literal: ${expression::class.simpleName}")
        }
}
