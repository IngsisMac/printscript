package com.printscript.parser

import com.printscript.ast.Expression
import com.printscript.common.Version
import com.printscript.parser.expression.infix.InfixParser
import com.printscript.parser.expression.prefix.PrefixParser

class ExpressionParser(
    private val prefixParsers: List<PrefixParser>,
    private val infixParsers: List<InfixParser>,
) {
    constructor(version: Version) : this(
        ExpressionParserFactory.createPrefixParsers(version),
        ExpressionParserFactory.createInfixParsers(),
    )

    fun parseExpression(
        stream: TokenStream,
        minBp: Int = 0,
    ): Expression {
        var left = parseUnaryExpression(stream)

        while (true) {
            val token = stream.peek()
            val infixParser = infixParsers.firstOrNull { it.matches(token) } ?: break
            if (infixParser.leftBindingPower < minBp) break

            stream.consume() // consume op token
            left = infixParser.parse(left, token, stream, this)
        }

        return left
    }

    fun parseUnaryExpression(stream: TokenStream): Expression {
        val token = stream.peek()
        val prefixParser =
            prefixParsers.firstOrNull { it.matches(token) }
                ?: throw ParseException("Unexpected token '${token.lexeme}' (${token.type})", token.span)

        stream.consume() // consume token
        return prefixParser.parse(token, stream, this)
    }
}
