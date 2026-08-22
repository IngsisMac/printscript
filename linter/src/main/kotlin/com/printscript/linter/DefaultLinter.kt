package com.printscript.linter

import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError
import com.printscript.linter.rule.IdentifierFormatRule
import com.printscript.linter.rule.LinterRule
import com.printscript.linter.rule.NoEmptyPrintlnRule
import com.printscript.linter.rule.NoUnusedVariablesRule
import com.printscript.linter.rule.PrintlnExpressionRule
import com.printscript.linter.rule.ReadInputExpressionRule
import com.printscript.linter.visitor.AstVisitorLinter

class DefaultLinter(
    private val rules: List<LinterRule> =
        listOf(
            IdentifierFormatRule(),
            PrintlnExpressionRule(),
            ReadInputExpressionRule(),
            NoEmptyPrintlnRule(),
            NoUnusedVariablesRule(),
        ),
) : Linter {
    override fun analyze(
        statements: Iterator<Statement>,
        config: LinterConfig,
        onError: (PrintScriptError) -> Unit,
    ): List<PrintScriptError> {
        rules.forEach { it.reset() }
        val visitor = AstVisitorLinter(rules, config)
        val errors = mutableListOf<PrintScriptError>()
        for (statement in statements) {
            val stmtErrors = statement.accept(visitor)
            for (error in stmtErrors) {
                onError(error)
                errors.add(error)
            }
        }
        for (rule in rules) {
            val endErrors = rule.finish(config)
            for (error in endErrors) {
                onError(error)
                errors.add(error)
            }
        }
        return errors
    }
}
