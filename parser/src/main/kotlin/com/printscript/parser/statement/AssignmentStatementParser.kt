package com.printscript.parser.statement

import com.printscript.ast.Assignment
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream
import com.printscript.token.TokenType

class AssignmentStatementParser : StatementParser {
    override fun matches(stream: TokenStream): Boolean = stream.check(TokenType.IDENTIFIER) && stream.peekNext().type == TokenType.EQUAL

    override fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement {
        val nameToken = stream.expect(TokenType.IDENTIFIER)
        stream.expect(TokenType.EQUAL)
        val value = parser.parseExpression()
        val endToken = stream.expect(TokenType.SEMICOLON)
        return Assignment(nameToken.lexeme, value, Span(nameToken.span.start, endToken.span.end))
    }
}
