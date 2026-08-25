package com.printscript.formatter.rule

import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.formatter.FormatterContext
import java.io.Writer

class PrintStatementFormattingRule : FormattingRule {
    override fun appliesTo(statement: Statement): Boolean = statement is PrintStatement

    override fun format(
        statement: Statement,
        writer: Writer,
        context: FormatterContext,
    ) {
        val printStmt = statement as PrintStatement
        val config = context.config
        val indent = context.indentString
        val exprStr = context.expressionFormatter.format(printStmt.expression, config)
        val semi = FormattingUtils.getSemicolon(config)
        val fnCall = if (config.mandatorySingleSpaceSeparation) "println ( $exprStr )" else "println($exprStr)"
        writer.write("$indent$fnCall$semi")
    }
}
