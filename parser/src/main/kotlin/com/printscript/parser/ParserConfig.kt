package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.expression.infix.InfixParser
import com.printscript.parser.expression.prefix.PrefixParser
import com.printscript.parser.statement.StatementParser

class ParserConfig(
    val statementParsers: List<StatementParser>,
    val prefixParsers: List<PrefixParser>,
    val infixParsers: List<InfixParser>,
    val version: Version,
) {
    companion object {
        fun from(version: Version): ParserConfig {
            val statementParsers = StatementParserFactory.create(version)
            val prefixParsers = ExpressionParserFactory.createPrefixParsers(version)
            val infixParsers = ExpressionParserFactory.createInfixParsers()
            return ParserConfig(statementParsers, prefixParsers, infixParsers, version)
        }
    }
}
