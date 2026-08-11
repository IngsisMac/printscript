package com.printscript.parser

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType

class Parser(
    tokens: Iterator<Token>,
    private val version: Version,
) {
    private val stream = TokenStream(tokens)
    private val statementParsers: List<StatementParser> =
        listOf(
            DeclarationStatementParser(version),
            AssignmentStatementParser(),
            PrintStatementParser(),
            IfStatementParser(version),
        )

    fun parse(): Iterator<Statement> =
        sequence {
            while (!stream.check(TokenType.EOF)) {
                val stmt = parseNextStatement()
                if (stmt != null) yield(stmt)
            }
        }.iterator()

    fun parseNextStatement(): Statement? {
        if (stream.check(TokenType.EOF)) return null

        val parser = statementParsers.firstOrNull { it.matches(stream) }
        if (parser != null) {
            return parser.parse(stream, this)
        }

        val token = stream.peek()
        throw ParseException("Unexpected token '${token.lexeme}' (${token.type})", token.span)
    }

    fun parseExpression(minBp: Int = 0): Expression {
        var left = parseUnaryExpression()

        while (true) {
            val op = getInfixOp(stream) ?: break
            if (op.leftBp < minBp) break

            stream.consume() // consume operator token
            val right = parseExpression(op.rightBp)
            left = BinaryOp(left, op.symbol, right, Span(left.span.start, right.span.end))
        }

        return left
    }

    private fun parseUnaryExpression(): Expression {
        if (stream.checkAny(TokenType.MINUS, TokenType.PLUS)) {
            val opToken = stream.consume()
            val operand = parseUnaryExpression()
            return BinaryOp(
                NumberLiteral("0", opToken.span),
                opToken.lexeme,
                operand,
                Span(opToken.span.start, operand.span.end),
            )
        }
        return parsePrimaryExpression()
    }

    private fun parsePrimaryExpression(): Expression {
        val token = stream.peek()
        return when (token.type) {
            TokenType.NUMBER_LITERAL -> {
                stream.consume()
                NumberLiteral(token.lexeme, token.span)
            }

            TokenType.STRING_LITERAL -> {
                stream.consume()
                StringLiteral(token.lexeme, token.span)
            }

            TokenType.TRUE -> {
                if (version == Version.V1_0) {
                    throw ParseException("boolean literal 'true' is not supported in version 1.0", token.span)
                }
                stream.consume()
                BooleanLiteral(true, token.span)
            }

            TokenType.FALSE -> {
                if (version == Version.V1_0) {
                    throw ParseException("boolean literal 'false' is not supported in version 1.0", token.span)
                }
                stream.consume()
                BooleanLiteral(false, token.span)
            }

            TokenType.IDENTIFIER -> {
                stream.consume()
                if (stream.match(TokenType.LPAREN)) {
                    val arg = if (!stream.check(TokenType.RPAREN)) parseExpression() else null
                    val endToken = stream.expect(TokenType.RPAREN)
                    CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
                } else {
                    Variable(token.lexeme, token.span)
                }
            }

            TokenType.READ_INPUT, TokenType.READ_ENV -> {
                if (version == Version.V1_0) {
                    throw ParseException("'${token.lexeme}' is not supported in version 1.0", token.span)
                }
                stream.consume()
                stream.expect(TokenType.LPAREN)
                val arg = if (!stream.check(TokenType.RPAREN)) parseExpression() else null
                val endToken = stream.expect(TokenType.RPAREN)
                CallExpression(token.lexeme, arg, Span(token.span.start, endToken.span.end))
            }

            TokenType.LPAREN -> {
                stream.consume()
                val expr = parseExpression()
                stream.expect(TokenType.RPAREN)
                expr
            }

            else -> throw ParseException("Unexpected token '${token.lexeme}' (${token.type})", token.span)
        }
    }

    private data class InfixOp(
        val leftBp: Int,
        val rightBp: Int,
        val symbol: String,
    )

    private fun getInfixOp(stream: TokenStream): InfixOp? {
        val token = stream.peek()
        return when (token.type) {
            TokenType.PLUS -> InfixOp(20, 21, "+")
            TokenType.MINUS -> InfixOp(20, 21, "-")
            TokenType.STAR -> InfixOp(30, 31, "*")
            TokenType.SLASH -> InfixOp(30, 31, "/")
            TokenType.IDENTIFIER -> {
                when (token.lexeme) {
                    "||" -> InfixOp(3, 4, "||")
                    "&&" -> InfixOp(5, 6, "&&")
                    "==" -> InfixOp(10, 11, "==")
                    else -> null
                }
            }
            else -> null
        }
    }
}

class ParseException(
    val rawMessage: String,
    val span: Span,
) : Exception(rawMessage)
