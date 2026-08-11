package com.printscript.parser

import com.printscript.ast.BinaryOp
import com.printscript.ast.Expression
import com.printscript.common.Span
import com.printscript.token.Token
import com.printscript.token.TokenType

interface InfixParser {
    val leftBindingPower: Int
    val rightBindingPower: Int

    fun matches(token: Token): Boolean

    fun parse(
        left: Expression,
        opToken: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression
}

class BinaryOperatorInfixParser(
    private val tokenType: TokenType?,
    private val lexemeSymbol: String?,
    override val leftBindingPower: Int,
    override val rightBindingPower: Int,
    private val symbol: String,
) : InfixParser {
    override fun matches(token: Token): Boolean {
        if (tokenType != null && token.type == tokenType) return true
        if (lexemeSymbol != null && token.type == TokenType.IDENTIFIER && token.lexeme == lexemeSymbol) return true
        return false
    }

    override fun parse(
        left: Expression,
        opToken: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        val right = expressionParser.parseExpression(stream, rightBindingPower)
        return BinaryOp(left, symbol, right, Span(left.span.start, right.span.end))
    }
}
