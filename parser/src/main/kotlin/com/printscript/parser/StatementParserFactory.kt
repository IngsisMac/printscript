package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.statement.AssignmentStatementParser
import com.printscript.parser.statement.DeclarationStatementParser
import com.printscript.parser.statement.IfStatementParser
import com.printscript.parser.statement.PrintStatementParser
import com.printscript.parser.statement.StatementParser

object StatementParserFactory {
    fun create(version: Version): List<StatementParser> =
        when (version) {
            Version.V1_0 ->
                listOf(
                    DeclarationStatementParser(version),
                    AssignmentStatementParser(),
                    PrintStatementParser(),
                )
            Version.V1_1 ->
                listOf(
                    DeclarationStatementParser(version),
                    AssignmentStatementParser(),
                    PrintStatementParser(),
                    IfStatementParser(),
                )
        }
}
