package com.printscript.formatter

import com.printscript.ast.Statement
import com.printscript.formatter.rule.AssignmentFormattingRule
import com.printscript.formatter.rule.DeclarationFormattingRule
import com.printscript.formatter.rule.FormattingRule
import com.printscript.formatter.rule.IfFormattingRule
import com.printscript.formatter.rule.PrintStatementFormattingRule
import java.io.Writer

class DefaultFormatter(
    private val rules: List<FormattingRule> = defaultRules(),
    private val expressionFormatter: ExpressionFormatter = DefaultExpressionFormatter(),
) : Formatter {
    override fun format(
        statement: Statement,
        writer: Writer,
        config: FormatterConfig,
        indentLevel: Int,
    ) {
        val context = FormatterContext(config, indentLevel, this, expressionFormatter)
        val rule =
            rules.firstOrNull { it.appliesTo(statement) }
                ?: error("No formatting rule found for statement: ${statement::class.simpleName}")

        rule.format(statement, writer, context)
    }

    companion object {
        fun defaultRules(): List<FormattingRule> =
            listOf(
                DeclarationFormattingRule(),
                AssignmentFormattingRule(),
                PrintStatementFormattingRule(),
                IfFormattingRule(),
            )
    }
}
