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
            BinaryOperatorInfixParser(TokenType.STAR, null, 30, 31, "*"),
            BinaryOperatorInfixParser(TokenType.SLASH, null, 30, 31, "/"),
            BinaryOperatorInfixParser(TokenType.PLUS, null, 20, 21, "+"),
            BinaryOperatorInfixParser(TokenType.MINUS, null, 20, 21, "-"),
        )

    fun create(version: Version): ExpressionParser = ExpressionParser(createPrefixParsers(version), createInfixParsers())
}
