package com.printscript.parser.expression.prefix

import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class NumberLiteralPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.NUMBER_LITERAL

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression = NumberLiteral(token.lexeme, token.span)
}
