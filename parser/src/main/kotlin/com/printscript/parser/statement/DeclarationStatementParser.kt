package com.printscript.parser.statement

import com.printscript.ast.Declaration
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream
import com.printscript.token.Token
import com.printscript.token.TokenType

class DeclarationStatementParser(
    private val version: Version = Version.V1_1,
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

    private fun parseTypeToken(stream: TokenStream): Token {
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
