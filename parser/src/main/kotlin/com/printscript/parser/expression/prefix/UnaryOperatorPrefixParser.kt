package com.printscript.parser.expression.prefix

import com.printscript.ast.BinaryOp
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.common.Span
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class UnaryOperatorPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.MINUS || token.type == TokenType.PLUS

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        val operand = expressionParser.parseUnaryExpression(stream)
        return BinaryOp(
            NumberLiteral("0", token.span),
            token.lexeme,
            operand,
            Span(token.span.start, operand.span.end),
        )
    }
}
