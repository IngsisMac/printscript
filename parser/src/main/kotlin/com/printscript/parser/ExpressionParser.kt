package com.printscript.parser

import com.printscript.ast.Expression
import com.printscript.common.Version
import com.printscript.token.TokenType

class ExpressionParser(
    version: Version,
) {
    private val prefixParsers: List<PrefixParser> =
        listOf(
            NumberLiteralPrefixParser(),
            StringLiteralPrefixParser(),
            BooleanLiteralPrefixParser(version),
            ReadFunctionPrefixParser(version),
            IdentifierOrCallPrefixParser(),
            UnaryOperatorPrefixParser(),
            GroupedExpressionPrefixParser(),
        )

    private val infixParsers: List<InfixParser> =
        listOf(
            BinaryOperatorInfixParser(TokenType.STAR, null, 30, 31, "*"),
            BinaryOperatorInfixParser(TokenType.SLASH, null, 30, 31, "/"),
            BinaryOperatorInfixParser(TokenType.PLUS, null, 20, 21, "+"),
            BinaryOperatorInfixParser(TokenType.MINUS, null, 20, 21, "-"),
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
