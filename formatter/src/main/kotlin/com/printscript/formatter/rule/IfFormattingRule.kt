package com.printscript.formatter.rule

import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.formatter.FormatterContext
import java.io.Writer

class IfFormattingRule : FormattingRule {
    override fun appliesTo(statement: Statement): Boolean = statement is IfStatement

    override fun format(
        statement: Statement,
        writer: Writer,
        context: FormatterContext,
    ) {
        val ifStmt = statement as IfStatement
        val config = context.config
        val indent = context.indentString
        val condStr = context.expressionFormatter.format(ifStmt.condition, config)

        writeIfHeader(writer, indent, condStr, config.ifBraceBelowLine)
        formatBlock(ifStmt.thenBranch, writer, context.nextIndent())
        writer.write("\n$indent}")
        formatElseBranch(ifStmt.elseBranch, writer, context)
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
        context: FormatterContext,
    ) {
        if (elseBranch == null) return
        val indent = context.indentString
        val prefix = if (context.config.ifBraceBelowLine) "\n${indent}else\n$indent{\n" else " else {\n"
        writer.write(prefix)
        formatBlock(elseBranch, writer, context.nextIndent())
        writer.write("\n$indent}")
    }

    private fun formatBlock(
        statements: List<Statement>,
        writer: Writer,
        context: FormatterContext,
    ) {
        var isFirst = true
        var prevStmt: Statement? = null
        for (stmt in statements) {
            if (!isFirst) {
                val extra = if (prevStmt is PrintStatement) context.config.lineBreaksAfterPrintln else 0
                repeat(1 + extra) { writer.write("\n") }
            }
            context.formatter.format(stmt, writer, context.config, context.indentLevel)
            isFirst = false
            prevStmt = stmt
        }
    }
}
