package com.printscript.parser

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType

class Parser(
    tokens: Iterator<Token>,
    private val version: Version,
) {
    private val stream = TokenStream(tokens)
    private val expressionParser = ExpressionParser(version)
    private val statementParsers: List<StatementParser> =
        listOf(
            DeclarationStatementParser(version),
            AssignmentStatementParser(),
            PrintStatementParser(),
            IfStatementParser(version),
        )

    fun parse(): Iterator<Statement> =
        sequence {
            while (!stream.check(TokenType.EOF)) {
                val stmt = parseNextStatement()
                if (stmt != null) yield(stmt)
            }
        }.iterator()

    fun parseNextStatement(): Statement? {
        if (stream.check(TokenType.EOF)) return null

        val parser = statementParsers.firstOrNull { it.matches(stream) }
        if (parser != null) {
            return parser.parse(stream, this)
        }

        val token = stream.peek()
        throw ParseException("Unexpected token '${token.lexeme}' (${token.type})", token.span)
    }

    fun parseExpression(minBp: Int = 0): Expression =
        expressionParser.parseExpression(stream, minBp)
}

class ParseException(
    val rawMessage: String,
    val span: Span,
) : Exception(rawMessage)
