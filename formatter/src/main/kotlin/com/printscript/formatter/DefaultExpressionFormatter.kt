package com.printscript.formatter

import com.printscript.ast.Expression
import com.printscript.formatter.expression.BinaryOpExpressionRule
import com.printscript.formatter.expression.CallExpressionRule
import com.printscript.formatter.expression.ExpressionRule
import com.printscript.formatter.expression.LiteralExpressionRule
import com.printscript.formatter.expression.VariableExpressionRule

class DefaultExpressionFormatter(
    private val rules: List<ExpressionRule> = defaultRules(),
) : ExpressionFormatter {
    override fun format(
        expression: Expression,
        config: FormatterConfig,
    ): String {
        val rule =
            rules.firstOrNull { it.appliesTo(expression) }
                ?: error("No expression formatting rule found for ${expression::class.simpleName}")
        return rule.format(expression, config, this)
    }

    companion object {
        fun defaultRules(): List<ExpressionRule> =
            listOf(
                LiteralExpressionRule(),
                VariableExpressionRule(),
                BinaryOpExpressionRule(),
                CallExpressionRule(),
            )
    }
}
