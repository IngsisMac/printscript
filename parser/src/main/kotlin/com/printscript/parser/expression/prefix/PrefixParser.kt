package com.printscript.parser.expression.prefix

import com.printscript.ast.Expression
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token

interface PrefixParser {
    fun matches(token: Token): Boolean

    fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression
}
