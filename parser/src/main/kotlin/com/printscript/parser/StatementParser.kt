package com.printscript.parser

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.TokenType

interface StatementParser {
    fun matches(stream: TokenStream): Boolean

    fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement
}

class DeclarationStatementParser(
    private val version: Version,
) : StatementParser {
    override fun matches(stream: TokenStream): Boolean {
        val token = stream.peek()
        return token.type == TokenType.LET ||
            token.type == TokenType.CONST ||
            (token.type == TokenType.IDENTIFIER && token.lexeme == "const")
    }

    override fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement {
        val keywordToken = stream.consume()
        val isConst = keywordToken.type == TokenType.CONST || keywordToken.lexeme == "const"

        if (isConst && version == Version.V1_0) {
            throw ParseException("const is not supported in version 1.0", keywordToken.span)
        }

        val name = stream.expect(TokenType.IDENTIFIER).lexeme
        stream.expect(TokenType.COLON)
        val typeToken = parseTypeToken(stream)

        val value =
            if (stream.match(TokenType.EQUAL)) {
                parser.parseExpression()
            } else {
                null
            }

        val endToken = stream.expect(TokenType.SEMICOLON)
        return Declaration(name, typeToken.lexeme, value, Span(keywordToken.span.start, endToken.span.end), isConst)
    }

    private fun parseTypeToken(stream: TokenStream): com.printscript.token.Token {
        val typeToken =
            if (stream.checkAny(TokenType.NUMBER, TokenType.STRING, TokenType.BOOLEAN)) {
                stream.consume()
            } else if (stream.check(TokenType.IDENTIFIER) && stream.peek().lexeme == "boolean") {
                stream.consume()
            } else {
                throw ParseException("Expected type (number, string, boolean)", stream.peek().span)
            }

        if (typeToken.lexeme == "boolean" && version == Version.V1_0) {
            throw ParseException("boolean type is not supported in version 1.0", typeToken.span)
        }
        return typeToken
    }
}

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

class IfStatementParser(
    private val version: Version,
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
    ): Pair<List<Statement>, com.printscript.token.Token> {
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
