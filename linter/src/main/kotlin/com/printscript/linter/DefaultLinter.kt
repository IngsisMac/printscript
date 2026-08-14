package com.printscript.linter

import com.printscript.ast.IfStatement
import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError
import com.printscript.linter.rule.IdentifierFormatRule
import com.printscript.linter.rule.LinterRule
import com.printscript.linter.rule.PrintlnExpressionRule
import com.printscript.linter.rule.ReadInputExpressionRule

class DefaultLinter(
    private val rules: List<LinterRule> =
        listOf(
            IdentifierFormatRule(),
            PrintlnExpressionRule(),
            ReadInputExpressionRule(),
        ),
) : Linter {
    override fun analyze(
        statements: Iterator<Statement>,
        config: LinterConfig,
    ): List<PrintScriptError> {
        val errors = mutableListOf<PrintScriptError>()
        for (statement in statements) {
            errors.addAll(analyzeStatement(statement, config))
        }
        return errors
    }

    private fun analyzeStatement(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        val errors = mutableListOf<PrintScriptError>()
        for (rule in rules) {
            errors.addAll(rule.check(statement, config))
        }

        if (statement is IfStatement) {
            for (thenStmt in statement.thenBranch) {
                errors.addAll(analyzeStatement(thenStmt, config))
            }
            statement.elseBranch?.let { elseStmts ->
                for (elseStmt in elseStmts) {
                    errors.addAll(analyzeStatement(elseStmt, config))
                }
            }
        }

        return errors
    }
}
