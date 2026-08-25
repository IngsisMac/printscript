package com.printscript.formatter.expression

import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

class CallExpressionRule : ExpressionRule {
    override fun appliesTo(expression: Expression): Boolean = expression is CallExpression

    override fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String {
        val call = expression as CallExpression
        val argStr = call.argument?.let { formatter.format(it, config) } ?: ""
        return "${call.name}($argStr)"
    }
}
