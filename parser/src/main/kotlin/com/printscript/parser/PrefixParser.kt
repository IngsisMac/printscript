package com.printscript.parser

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType

interface PrefixParser {
    fun matches(token: Token): Boolean

    fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression
}

class NumberLiteralPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.NUMBER_LITERAL

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression = NumberLiteral(token.lexeme, token.span)
}

class StringLiteralPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.STRING_LITERAL

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression = StringLiteral(token.lexeme, token.span)
}

class BooleanLiteralPrefixParser(
    private val version: Version,
) : PrefixParser {
    override fun matches(token: Token): Boolean =
        token.type == TokenType.TRUE || token.type == TokenType.FALSE

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (version == Version.V1_0) {
            throw ParseException("boolean literal '${token.lexeme}' is not supported in version 1.0", token.span)
        }
        return BooleanLiteral(token.type == TokenType.TRUE, token.span)
    }
}

class IdentifierOrCallPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.IDENTIFIER

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (stream.match(TokenType.LPAREN)) {
            val arg = if (!stream.check(TokenType.RPAREN)) expressionParser.parseExpression(stream) else null
            val endToken = stream.expect(TokenType.RPAREN)
            return CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
        }
        return Variable(token.lexeme, token.span)
    }
}

class ReadFunctionPrefixParser(
    private val version: Version,
) : PrefixParser {
    override fun matches(token: Token): Boolean =
        token.type == TokenType.READ_INPUT || token.type == TokenType.READ_ENV ||
            (token.type == TokenType.IDENTIFIER && (token.lexeme == "readInput" || token.lexeme == "readEnv"))

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        if (version == Version.V1_0) {
            throw ParseException("'${token.lexeme}' is not supported in version 1.0", token.span)
        }
        stream.expect(TokenType.LPAREN)
        val arg = if (!stream.check(TokenType.RPAREN)) expressionParser.parseExpression(stream) else null
        val endToken = stream.expect(TokenType.RPAREN)
        return CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
    }
}

class UnaryOperatorPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean =
        token.type == TokenType.MINUS || token.type == TokenType.PLUS

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        val operand = expressionParser.parseUnaryExpression(stream)
        return BinaryOp(
            NumberLiteral("0", token.span),
            token.lexeme,
            operand,
            Span(token.span.start, operand.span.end),
        )
    }
}

class GroupedExpressionPrefixParser : PrefixParser {
    override fun matches(token: Token): Boolean = token.type == TokenType.LPAREN

    override fun parse(
        token: Token,
        stream: TokenStream,
        expressionParser: ExpressionParser,
    ): Expression {
        val expr = expressionParser.parseExpression(stream)
        stream.expect(TokenType.RPAREN)
        return expr
    }
}
