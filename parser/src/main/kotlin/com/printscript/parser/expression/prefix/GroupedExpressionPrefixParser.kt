package com.printscript.parser.expression.prefix

import com.printscript.ast.Expression
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class GroupedExpressionPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.LPAREN

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        val expr = expressionParser.parseExpression(stream)
        stream.expect(TokenType.RPAREN)
        return expr
    }
}
