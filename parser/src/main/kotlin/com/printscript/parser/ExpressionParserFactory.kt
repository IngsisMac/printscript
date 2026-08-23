package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.expression.infix.BinaryOperatorInfixParser
import com.printscript.parser.expression.infix.InfixParser
import com.printscript.parser.expression.prefix.BooleanLiteralPrefixParser
import com.printscript.parser.expression.prefix.GroupedExpressionPrefixParser
import com.printscript.parser.expression.prefix.IdentifierOrCallPrefixParser
import com.printscript.parser.expression.prefix.NumberLiteralPrefixParser
import com.printscript.parser.expression.prefix.PrefixParser
import com.printscript.parser.expression.prefix.ReadFunctionPrefixParser
import com.printscript.parser.expression.prefix.StringLiteralPrefixParser
import com.printscript.parser.expression.prefix.UnaryOperatorPrefixParser
import com.printscript.token.TokenType

private const val MULTIPLICATIVE_PRECEDENCE = 30
private const val MULTIPLICATIVE_RIGHT_PRECEDENCE = 31
private const val ADDITIVE_PRECEDENCE = 20
private const val ADDITIVE_RIGHT_PRECEDENCE = 21

object ExpressionParserFactory {
    fun createPrefixParsers(version: Version): List<PrefixParser> =
        when (version) {
            Version.V1_0 -> createV10PrefixParsers(version)
            Version.V1_1 -> createV11PrefixParsers(version)
        }

    private fun createV10PrefixParsers(version: Version): List<PrefixParser> =
        listOf(
            NumberLiteralPrefixParser(),
            StringLiteralPrefixParser(),
            BooleanLiteralPrefixParser(version),
            ReadFunctionPrefixParser(version),
            IdentifierOrCallPrefixParser(),
            UnaryOperatorPrefixParser(),
            GroupedExpressionPrefixParser(),
        )

    private fun createV11PrefixParsers(version: Version): List<PrefixParser> =
        listOf(
            NumberLiteralPrefixParser(),
            StringLiteralPrefixParser(),
            BooleanLiteralPrefixParser(version),
            ReadFunctionPrefixParser(version),
            IdentifierOrCallPrefixParser(),
            UnaryOperatorPrefixParser(),
            GroupedExpressionPrefixParser(),
        )

    fun createInfixParsers(): List<InfixParser> =
        listOf(
            createBinaryParser(TokenType.STAR, "*", MULTIPLICATIVE_PRECEDENCE, MULTIPLICATIVE_RIGHT_PRECEDENCE),
            createBinaryParser(TokenType.SLASH, "/", MULTIPLICATIVE_PRECEDENCE, MULTIPLICATIVE_RIGHT_PRECEDENCE),
            createBinaryParser(TokenType.PLUS, "+", ADDITIVE_PRECEDENCE, ADDITIVE_RIGHT_PRECEDENCE),
            createBinaryParser(TokenType.MINUS, "-", ADDITIVE_PRECEDENCE, ADDITIVE_RIGHT_PRECEDENCE),
        )

    private fun createBinaryParser(
        type: TokenType,
        symbol: String,
        leftBp: Int,
        rightBp: Int,
    ): InfixParser = BinaryOperatorInfixParser(type, null, leftBp, rightBp, symbol)

    fun create(version: Version): ExpressionParser =
        ExpressionParser(createPrefixParsers(version), createInfixParsers())
}
