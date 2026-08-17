package com.printscript.parser.expression.infix

import com.printscript.ast.Expression
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token

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
