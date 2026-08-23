package com.printscript.formatter

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import java.io.Writer

class DefaultFormatter : Formatter {
    override fun format(
        statement: Statement,
        writer: Writer,
        config: FormatterConfig,
        indentLevel: Int,
    ) {
        val indent = " ".repeat(indentLevel * config.indentInsideIf)
        when (statement) {
            is Declaration -> formatDeclaration(statement, writer, config, indent)
            is Assignment -> formatAssignment(statement, writer, config, indent)
            is PrintStatement -> formatPrintStatement(statement, writer, config, indent)
            is IfStatement -> formatIfStatement(statement, writer, config, indentLevel)
        }
    }

    private fun formatDeclaration(
        declaration: Declaration,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
    ) {
        val kw = if (declaration.isConst) "const" else "let"
        val b =
            getColonSpacing(config.enforceSpacingBeforeColonInDeclaration, declaration.spaceBeforeColon, false, config)
        val a = getColonSpacing(config.enforceSpacingAfterColonInDeclaration, declaration.spaceAfterColon, true, config)
        val head = "$indent$kw ${declaration.name}$b:$a${declaration.type}"
        val valuePart =
            declaration.value?.let {
                val eq = getEqualsSpacing(config.enforceSpacingAroundEquals, declaration.spaceAroundEquals, config)
                "$eq${ExpressionFormatter.format(it, config)}"
            } ?: ""
        writer.write("$head$valuePart${getSemicolon(config)}")
    }

    private fun formatAssignment(
        assignment: Assignment,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
    ) {
        val eq = getEqualsSpacing(config.enforceSpacingAroundEquals, assignment.spaceAroundEquals, config)
        val valueStr = ExpressionFormatter.format(assignment.value, config)
        writer.write("$indent${assignment.name}$eq$valueStr${getSemicolon(config)}")
    }

    private fun formatPrintStatement(
        printStmt: PrintStatement,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
    ) {
        val exprStr = ExpressionFormatter.format(printStmt.expression, config)
        val semi = getSemicolon(config)
        val fnCall = if (config.mandatorySingleSpaceSeparation) "println ( $exprStr )" else "println($exprStr)"
        writer.write("$indent$fnCall$semi")
    }

    private fun formatIfStatement(
        ifStmt: IfStatement,
        writer: Writer,
        config: FormatterConfig,
        indentLevel: Int,
    ) {
        val indent = " ".repeat(indentLevel * config.indentInsideIf)
        val condStr = ExpressionFormatter.format(ifStmt.condition, config)
        writeIfHeader(writer, indent, condStr, config.ifBraceBelowLine)
        formatBlock(ifStmt.thenBranch, writer, config, indentLevel)
        writer.write("\n$indent}")
        formatElseBranch(ifStmt.elseBranch, writer, config, indent, indentLevel)
    }

    private fun writeIfHeader(
        writer: Writer,
        indent: String,
        condStr: String,
        belowLine: Boolean,
    ) {
        val header = if (belowLine) "${indent}if ($condStr)\n$indent{\n" else "${indent}if ($condStr) {\n"
        writer.write(header)
    }

    private fun formatElseBranch(
        elseBranch: List<Statement>?,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
        indentLevel: Int,
    ) {
        if (elseBranch == null) return
        val prefix = if (config.ifBraceBelowLine) "\n${indent}else\n$indent{\n" else " else {\n"
        writer.write(prefix)
        formatBlock(elseBranch, writer, config, indentLevel)
        writer.write("\n$indent}")
    }

    private fun formatBlock(
        statements: List<Statement>,
        writer: Writer,
        config: FormatterConfig,
        indentLevel: Int,
    ) {
        var isFirst = true
        var prevStmt: Statement? = null
        for (stmt in statements) {
            if (!isFirst) {
                val extra = if (prevStmt is PrintStatement) config.lineBreaksAfterPrintln else 0
                repeat(1 + extra) { writer.write("\n") }
            }
            format(stmt, writer, config, indentLevel + 1)
            isFirst = false
            prevStmt = stmt
        }
    }

    private fun getColonSpacing(
        enforceConfig: Boolean?,
        astSpace: Boolean?,
        defaultSpace: Boolean,
        config: FormatterConfig,
    ): String {
        if (config.mandatorySingleSpaceSeparation) return " "
        val space = enforceConfig ?: astSpace ?: defaultSpace
        return if (space) " " else ""
    }

    private fun getEqualsSpacing(
        enforceConfig: Boolean?,
        astSpace: Boolean?,
        config: FormatterConfig,
    ): String {
        if (config.mandatorySingleSpaceSeparation) return " = "
        val space = enforceConfig ?: astSpace ?: true
        return if (space) " = " else "="
    }

    private fun getSemicolon(config: FormatterConfig): String = if (config.enforceNoSpaceBeforeSemicolon) ";" else " ;"
}
