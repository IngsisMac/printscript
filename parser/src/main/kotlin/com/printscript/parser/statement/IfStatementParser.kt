package com.printscript.parser.statement

import com.printscript.ast.IfStatement
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class IfStatementParser(
    private val version: Version = Version.V1_1,
) : StatementParser {
    override fun matches(stream: TokenStream): Boolean {
        val token = stream.peek()
        return token.type == TokenType.IF || (token.type == TokenType.IDENTIFIER && token.lexeme == "if")
    }

    override fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement {
        val startToken = stream.consume()
        if (version == Version.V1_0 && startToken.lexeme == "if") {
            throw ParseException("if statements are not supported in version 1.0", startToken.span)
        }

        stream.expect(TokenType.LPAREN)
        val condition = parser.parseExpression()
        stream.expect(TokenType.RPAREN)

        val (thenBranch, thenEnd) = parseBlock(stream, parser)
        val elseResult = parseElseBranch(stream, parser)
        val elseBranch = elseResult?.first
        val lastEndSpan = elseResult?.second ?: thenEnd.span

        return IfStatement(condition, thenBranch, elseBranch, Span(startToken.span.start, lastEndSpan.end))
    }

    private fun parseElseBranch(
        stream: TokenStream,
        parser: Parser,
    ): Pair<List<Statement>?, Span>? {
        if (!stream.match(TokenType.ELSE)) return null
        if (stream.check(TokenType.IF)) {
            throw ParseException("else if is not supported", stream.peek().span)
        }
        val (elseBody, elseEnd) = parseBlock(stream, parser)
        return elseBody to elseEnd.span
    }

    private fun parseBlock(
        stream: TokenStream,
        parser: Parser,
    ): Pair<List<Statement>, Token> {
        stream.expect(TokenType.LBRACE)
        val body = mutableListOf<Statement>()
        while (!stream.check(TokenType.RBRACE) && !stream.check(TokenType.EOF)) {
            val stmt = parser.parseNextStatement()
            if (stmt != null) body.add(stmt)
        }
        val endToken = stream.expect(TokenType.RBRACE)
        return body to endToken
    }
}
