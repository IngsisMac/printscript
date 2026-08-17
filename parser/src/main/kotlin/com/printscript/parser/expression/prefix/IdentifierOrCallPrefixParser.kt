package com.printscript.parser.expression.prefix

import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.Variable
import com.printscript.common.Span
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class IdentifierOrCallPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.IDENTIFIER

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (stream.match(TokenType.LPAREN)) {
            val arg = if (!stream.check(TokenType.RPAREN)) expressionParser.parseExpression(stream) else null
            val endToken = stream.expect(TokenType.RPAREN)
            return CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
        }
        return Variable(token.lexeme, token.span)
    }
}
