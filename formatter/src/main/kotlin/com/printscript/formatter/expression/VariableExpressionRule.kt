package com.printscript.formatter.expression

import com.printscript.ast.Expression
import com.printscript.ast.Variable
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

class VariableExpressionRule : ExpressionRule {
    override fun appliesTo(expression: Expression): Boolean = expression is Variable

    override fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String = (expression as Variable).name
}
