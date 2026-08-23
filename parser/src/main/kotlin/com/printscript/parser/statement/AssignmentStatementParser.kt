package com.printscript.parser.statement

import com.printscript.ast.Assignment
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class AssignmentStatementParser : StatementParser {
    override fun matches(stream: TokenStream): Boolean =
        stream.check(TokenType.IDENTIFIER) && stream.peekNext().type == TokenType.EQUAL

    override fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement {
        val nameToken = stream.expect(TokenType.IDENTIFIER)
        val equalToken = stream.expect(TokenType.EQUAL)
        val spaceAroundEquals = hasSpace(nameToken, equalToken) && hasSpace(equalToken, stream.peek())
        val value = parser.parseExpression()
        val endToken = stream.expect(TokenType.SEMICOLON)
        return Assignment(
            name = nameToken.lexeme,
            value = value,
            span = Span(nameToken.span.start, endToken.span.end),
            nameSpan = nameToken.span,
            spaceAroundEquals = spaceAroundEquals,
        )
    }

    private fun hasSpace(
        first: Token,
        second: Token,
    ): Boolean =
        second.span.start.line > first.span.end.line ||
            second.span.start.column > first.span.end.column + 1
}
