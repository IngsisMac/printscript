package com.printscript.parser.expression.prefix

import com.printscript.ast.Expression
import com.printscript.ast.StringLiteral
import com.printscript.parser.ExpressionParser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class StringLiteralPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.STRING_LITERAL

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression = StringLiteral(token.lexeme, token.span)
}
