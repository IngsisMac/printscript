package com.printscript.parser.statement

import com.printscript.ast.Declaration
import com.printscript.ast.Expression
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
        val (kwToken, isConst) = parseKeyword(stream)
        val nameToken = stream.expect(TokenType.IDENTIFIER)
        val colonToken = stream.expect(TokenType.COLON)
        val typeToken = parseTypeToken(stream)
        val (value, spaceAroundEq) = parseInitializer(stream, parser, typeToken)
        val endToken = stream.expect(TokenType.SEMICOLON)

        return Declaration(
            name = nameToken.lexeme,
            type = typeToken.lexeme,
            value = value,
            span = Span(kwToken.span.start, endToken.span.end),
            isConst = isConst,
            nameSpan = nameToken.span,
            spaceBeforeColon = hasSpace(nameToken, colonToken),
            spaceAfterColon = hasSpace(colonToken, typeToken),
            spaceAroundEquals = spaceAroundEq,
        )
    }

    private fun parseKeyword(stream: TokenStream): Pair<Token, Boolean> {
        val kwToken = stream.consume()
        val isConst = kwToken.type == TokenType.CONST || kwToken.lexeme == "const"
        if (isConst && version == Version.V1_0) {
            throw ParseException("const is not supported in version 1.0", kwToken.span)
        }
        return Pair(kwToken, isConst)
    }

    private fun parseInitializer(
        stream: TokenStream,
        parser: Parser,
        typeToken: Token,
    ): Pair<Expression?, Boolean?> {
        if (!stream.check(TokenType.EQUAL)) return Pair(null, null)
        val eqToken = stream.consume()
        val spaceAround = hasSpace(typeToken, eqToken) && hasSpace(eqToken, stream.peek())
        return Pair(parser.parseExpression(), spaceAround)
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

    private fun hasSpace(
        first: Token,
        second: Token,
    ): Boolean =
        second.span.start.line > first.span.end.line ||
            second.span.start.column > first.span.end.column + 1
}
