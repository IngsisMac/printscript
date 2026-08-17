package com.printscript.parser.expression.prefix

import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.parser.ExpressionParser
import com.printscript.parser.ParseException
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class ReadFunctionPrefixParser(
    private val version: Version = Version.V1_1,
) : PrefixParser {
    override fun matches(token: Token): Boolean =
        token.type == TokenType.READ_INPUT ||
            token.type == TokenType.READ_ENV ||
            (token.type == TokenType.IDENTIFIER && (token.lexeme == "readInput" || token.lexeme == "readEnv"))

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (version == Version.V1_0) {
            throw ParseException("'${token.lexeme}' is not supported in version 1.0", token.span)
        }
        stream.expect(TokenType.LPAREN)
        val arg = if (!stream.check(TokenType.RPAREN)) expressionParser.parseExpression(stream) else null
        val endToken = stream.expect(TokenType.RPAREN)
        return CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
    }
}
