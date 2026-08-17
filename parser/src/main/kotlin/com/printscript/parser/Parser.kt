package com.printscript.parser

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.parser.statement.StatementParser
import com.printscript.token.Token
import com.printscript.token.TokenType

class Parser(
    tokens: Iterator<Token>,
    private val statementParsers: List<StatementParser>,
    private val expressionParser: ExpressionParser,
) {
    private val stream = TokenStream(tokens)

    constructor(tokens: Iterator<Token>, config: ParserConfig) : this(
        tokens,
        config.statementParsers,
        ExpressionParser(config.prefixParsers, config.infixParsers),
    )

    constructor(tokens: Iterator<Token>, version: Version) : this(tokens, ParserConfig.from(version))

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
        if (token.lexeme == "if") {
            throw ParseException("if statements are not supported in version 1.0", token.span)
        }
        throw ParseException("Unexpected token '${token.lexeme}' (${token.type})", token.span)
    }

    fun parseExpression(minBp: Int = 0): Expression = expressionParser.parseExpression(stream, minBp)
}

class ParseException(
    val rawMessage: String,
    val span: Span,
) : Exception(rawMessage)
