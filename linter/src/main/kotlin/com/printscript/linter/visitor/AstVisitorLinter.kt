package com.printscript.linter.visitor

import com.printscript.ast.Assignment
import com.printscript.ast.AstVisitor
import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.PrintScriptError
import com.printscript.linter.LinterConfig
import com.printscript.linter.rule.LinterRule

class AstVisitorLinter(
    private val rules: List<LinterRule>,
    private val config: LinterConfig,
) : AstVisitor<List<PrintScriptError>> {

    override fun visit(node: Declaration): List<PrintScriptError> = runRules(node)

    override fun visit(node: Assignment): List<PrintScriptError> = runRules(node)

    override fun visit(node: PrintStatement): List<PrintScriptError> = runRules(node)

    override fun visit(node: IfStatement): List<PrintScriptError> {
        val errors = mutableListOf<PrintScriptError>()
        errors.addAll(runRules(node))
        for (thenStmt in node.thenBranch) {
            errors.addAll(thenStmt.accept(this))
        }
        node.elseBranch?.let { elseStmts ->
            for (elseStmt in elseStmts) {
                errors.addAll(elseStmt.accept(this))
            }
        }
        return errors
    }

    override fun visit(node: NumberLiteral): List<PrintScriptError> = emptyList()

    override fun visit(node: StringLiteral): List<PrintScriptError> = emptyList()

    override fun visit(node: BooleanLiteral): List<PrintScriptError> = emptyList()

    override fun visit(node: Variable): List<PrintScriptError> = emptyList()

    override fun visit(node: BinaryOp): List<PrintScriptError> = emptyList()

    override fun visit(node: CallExpression): List<PrintScriptError> = emptyList()

    private fun runRules(statement: Statement): List<PrintScriptError> {
        val errors = mutableListOf<PrintScriptError>()
        for (rule in rules) {
            errors.addAll(rule.check(statement, config))
        }
        return errors
    }
}
