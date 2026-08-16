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
        val keyword = if (declaration.isConst) "const" else "let"
        val spaceBeforeColon = if (config.enforceSpacingBeforeColonInDeclaration) " " else ""
        val spaceAfterColon = if (config.enforceSpacingAfterColonInDeclaration) " " else ""
        val kwSpace = if (config.mandatorySingleSpaceSeparation) " " else ""
        val head = "$indent$keyword$kwSpace${declaration.name}$spaceBeforeColon:$spaceAfterColon${declaration.type}"

        if (declaration.value != null) {
            val equalsSpace = if (config.enforceSpacingAroundEquals) " = " else "="
            val valueStr = ExpressionFormatter.format(declaration.value!!, config)
            writer.write("$head$equalsSpace$valueStr;")
        } else {
            writer.write("$head;")
        }

        if (config.mandatoryLineBreakAfterStatement) {
            writer.write("\n")
        }
    }

    private fun formatAssignment(
        assignment: Assignment,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
    ) {
        val equalsSpace = if (config.enforceSpacingAroundEquals) " = " else "="
        val valueStr = ExpressionFormatter.format(assignment.value, config)

        writer.write("$indent${assignment.name}$equalsSpace$valueStr;")

        if (config.mandatoryLineBreakAfterStatement) {
            writer.write("\n")
        }
    }

    private fun formatPrintStatement(
        printStmt: PrintStatement,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
    ) {
        val exprStr = ExpressionFormatter.format(printStmt.expression, config)
        writer.write("${indent}println($exprStr);")

        // Mandatory line break for the statement itself
        if (config.mandatoryLineBreakAfterStatement) {
            writer.write("\n")
        }

        // Additional line breaks configured for println
        val extraNewlines = (config.lineBreaksAfterPrintln - 1).coerceAtLeast(0)
        repeat(extraNewlines) {
            writer.write("\n")
        }
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
        ifStmt.thenBranch.forEach { format(it, writer, config, indentLevel + 1) }
        writer.write("$indent}")

        formatElseBranch(ifStmt.elseBranch, writer, config, indent, indentLevel)

        if (config.mandatoryLineBreakAfterStatement) {
            writer.write("\n")
        }
    }

    private fun writeIfHeader(
        writer: Writer,
        indent: String,
        condStr: String,
        belowLine: Boolean,
    ) {
        if (belowLine) {
            writer.write("${indent}if ($condStr)\n$indent{\n")
        } else {
            writer.write("${indent}if ($condStr) {\n")
        }
    }

    private fun formatElseBranch(
        elseBranch: List<com.printscript.ast.Statement>?,
        writer: Writer,
        config: FormatterConfig,
        indent: String,
        indentLevel: Int,
    ) {
        if (elseBranch == null) return
        if (config.ifBraceBelowLine) {
            writer.write("\n${indent}else\n$indent{\n")
        } else {
            writer.write(" else {\n")
        }
        elseBranch.forEach { format(it, writer, config, indentLevel + 1) }
        writer.write("$indent}")
    }
}
