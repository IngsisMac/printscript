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
    private val ruleSupplier: () -> List<LinterRule> = { defaultRules() },
) : Linter {
    constructor(rules: List<LinterRule>) : this({ rules })

    override fun analyze(
        statements: Iterator<Statement>,
        config: LinterConfig,
        onError: (PrintScriptError) -> Unit,
    ): List<PrintScriptError> {
        val rules = ruleSupplier()
        val visitor = AstVisitorLinter(rules, config)
        val errors = mutableListOf<PrintScriptError>()
        processStatements(statements, visitor, errors, onError)
        collectFinishedErrors(rules, config, errors, onError)
        return errors
    }

    private fun processStatements(
        statements: Iterator<Statement>,
        visitor: AstVisitorLinter,
        errors: MutableList<PrintScriptError>,
        onError: (PrintScriptError) -> Unit,
    ) {
        for (statement in statements) {
            for (error in statement.accept(visitor)) {
                onError(error)
                errors.add(error)
            }
        }
    }

    private fun collectFinishedErrors(
        rules: List<LinterRule>,
        config: LinterConfig,
        errors: MutableList<PrintScriptError>,
        onError: (PrintScriptError) -> Unit,
    ) {
        for (rule in rules) {
            for (error in rule.finish(config)) {
                onError(error)
                errors.add(error)
            }
        }
    }

    companion object {
        fun defaultRules(): List<LinterRule> =
            listOf(
                IdentifierFormatRule(),
                PrintlnExpressionRule(),
                ReadInputExpressionRule(),
                NoEmptyPrintlnRule(),
                NoUnusedVariablesRule(),
            )
    }
}
