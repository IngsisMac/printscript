package com.printscript.parser.statement

import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream
import com.printscript.token.TokenType

class PrintStatementParser : StatementParser {
    override fun matches(stream: TokenStream): Boolean = stream.check(TokenType.PRINTLN)

    override fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement {
        val startToken = stream.expect(TokenType.PRINTLN)
        stream.expect(TokenType.LPAREN)
        val expr = parser.parseExpression()
        stream.expect(TokenType.RPAREN)
        val endToken = stream.expect(TokenType.SEMICOLON)
        return PrintStatement(expr, Span(startToken.span.start, endToken.span.end))
    }
}
