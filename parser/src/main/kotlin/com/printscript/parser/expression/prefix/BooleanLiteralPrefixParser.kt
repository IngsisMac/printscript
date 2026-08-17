package com.printscript.parser.expression.prefix

import com.printscript.ast.BooleanLiteral
import com.printscript.ast.Expression
import com.printscript.common.Version
import com.printscript.parser.ExpressionParser
import com.printscript.parser.ParseException
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class BooleanLiteralPrefixParser(
    private val version: Version = Version.V1_1,
) : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.TRUE || token.type == TokenType.FALSE

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (version == Version.V1_0) {
            throw ParseException("boolean literal '${token.lexeme}' is not supported in version 1.0", token.span)
        }
        return BooleanLiteral(token.type == TokenType.TRUE, token.span)
    }
}
