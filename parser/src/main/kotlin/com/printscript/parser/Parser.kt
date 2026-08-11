package com.printscript.parser

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.Expression
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType

class Parser(
    private val tokens: Iterator<Token>,
    private val version: Version,
) {
    private var currentToken: Token? = null
    private var nextToken: Token? = null

    init {
        advance()
        advance()
    }

    fun parse(): Iterator<Statement> =
        sequence {
            while (currentToken?.type != TokenType.EOF && currentToken != null) {
                val stmt = statement()
                if (stmt != null) yield(stmt)
            }
        }.iterator()

    private fun statement(): Statement? =
        when (currentToken?.type) {
            TokenType.LET -> declaration()
            TokenType.CONST -> {
                if (version == Version.V1_0) {
                    throw ParseException("const is not supported in version 1.0", currentToken!!.span)
                }
                declaration()
            }
            TokenType.PRINTLN -> printStatement()
            TokenType.IF -> {
                if (version == Version.V1_0) {
                    throw ParseException("if statements are not supported in version 1.0", currentToken!!.span)
                }
                ifStatement()
            }
            TokenType.IDENTIFIER -> {
                // Could be assignment or expression statement
                val saved = saveState()
                val name = currentToken!!.lexeme
                advance()
                if (currentToken?.type == TokenType.EQUAL) {
                    restoreState(saved)
                    assignment()
                } else {
                    restoreState(saved)
                    null // Expression statements not supported yet
                }
            }
            else -> {
                advance()
                null
            }
        }

    private fun declaration(): Statement {
        val startSpan = currentToken!!.span
        advance() // consume let/const
        val name = expect(TokenType.IDENTIFIER).lexeme
        expect(TokenType.COLON)
        val type = expect(TokenType.NUMBER, TokenType.STRING, TokenType.BOOLEAN).lexeme

        val value =
            if (currentToken?.type == TokenType.EQUAL) {
                advance()
                expression()
            } else {
                null
            }

        val endSpan = currentToken?.span ?: startSpan
        expect(TokenType.SEMICOLON)

        return Declaration(name, type, value, Span(startSpan.start, endSpan.end))
    }

    private fun assignment(): Statement {
        val startSpan = currentToken!!.span
        val name = expect(TokenType.IDENTIFIER).lexeme
        expect(TokenType.EQUAL)
        val value = expression()
        val endSpan = currentToken?.span ?: startSpan
        expect(TokenType.SEMICOLON)

        return Assignment(name, value, Span(startSpan.start, endSpan.end))
    }

    private fun printStatement(): Statement {
        val startSpan = currentToken!!.span
        advance() // consume println
        expect(TokenType.LPAREN)
        val expr = expression()
        expect(TokenType.RPAREN)
        val endSpan = currentToken?.span ?: startSpan
        expect(TokenType.SEMICOLON)

        return PrintStatement(expr, Span(startSpan.start, endSpan.end))
    }

    private fun ifStatement(): Statement {
        val startSpan = currentToken!!.span
        advance() // consume if
        expect(TokenType.LPAREN)
        val condition = expression()
        expect(TokenType.RPAREN)
        expect(TokenType.LBRACE)

        val thenBranch = mutableListOf<Statement>()
        while (currentToken?.type != TokenType.RBRACE && currentToken != null) {
            statement()?.let { thenBranch.add(it) }
        }
        expect(TokenType.RBRACE)

        val elseBranch =
            if (currentToken?.type == TokenType.ELSE) {
                advance()
                expect(TokenType.LBRACE)
                val elseBody = mutableListOf<Statement>()
                while (currentToken?.type != TokenType.RBRACE && currentToken != null) {
                    statement()?.let { elseBody.add(it) }
                }
                expect(TokenType.RBRACE)
                elseBody
            } else {
                null
            }

        val endSpan = currentToken?.span ?: startSpan
        return IfStatement(condition, thenBranch, elseBranch, Span(startSpan.start, endSpan.end))
    }

    private fun expression(): Expression = logicalOr()

    private fun logicalOr(): Expression {
        var left = logicalAnd()
        while (currentToken?.type in listOf(TokenType.IDENTIFIER)) {
            if (currentToken?.lexeme != "||") break
            val op = currentToken!!.lexeme
            advance()
            val right = logicalAnd()
            left = BinaryOp(left, op, right, Span(left.span.start, right.span.end))
        }
        return left
    }

    private fun logicalAnd(): Expression {
        var left = equality()
        while (currentToken?.type in listOf(TokenType.IDENTIFIER)) {
            if (currentToken?.lexeme != "&&") break
            val op = currentToken!!.lexeme
            advance()
            val right = equality()
            left = BinaryOp(left, op, right, Span(left.span.start, right.span.end))
        }
        return left
    }

    private fun equality(): Expression {
        var left = additive()
        while (currentToken?.type in listOf(TokenType.EQUAL)) {
            val op = currentToken!!.lexeme
            advance()
            val right = additive()
            left = BinaryOp(left, op, right, Span(left.span.start, right.span.end))
        }
        return left
    }

    private fun additive(): Expression {
        var left = multiplicative()
        while (currentToken?.type in listOf(TokenType.PLUS, TokenType.MINUS)) {
            val op = currentToken!!.lexeme
            advance()
            val right = multiplicative()
            left = BinaryOp(left, op, right, Span(left.span.start, right.span.end))
        }
        return left
    }

    private fun multiplicative(): Expression {
        var left = unary()
        while (currentToken?.type in listOf(TokenType.STAR, TokenType.SLASH)) {
            val op = currentToken!!.lexeme
            advance()
            val right = unary()
            left = BinaryOp(left, op, right, Span(left.span.start, right.span.end))
        }
        return left
    }

    private fun unary(): Expression =
        if (currentToken?.type in listOf(TokenType.MINUS, TokenType.PLUS)) {
            val op = currentToken!!.lexeme
            val opSpan = currentToken!!.span
            advance()
            val expr = unary()
            BinaryOp(NumberLiteral("0", opSpan), op, expr, Span(opSpan.start, expr.span.end))
        } else {
            call()
        }

    private fun call(): Expression {
        var expr = primary()
        while (currentToken?.type == TokenType.LPAREN) {
            advance()
            val arg = if (currentToken?.type != TokenType.RPAREN) expression() else null
            val endPos = expect(TokenType.RPAREN).span
            expr = CallExpression((expr as? Variable)?.name ?: "unknown", arg, Span(expr.span.start, endPos.end))
        }
        return expr
    }

    private fun primary(): Expression {
        val span = currentToken?.span ?: Span(Position(1, 1), Position(1, 1))
        return when (currentToken?.type) {
            TokenType.NUMBER_LITERAL -> {
                val value = currentToken!!.lexeme
                advance()
                NumberLiteral(value, span)
            }
            TokenType.STRING_LITERAL -> {
                val value = currentToken!!.lexeme
                advance()
                StringLiteral(value, span)
            }
            TokenType.TRUE -> {
                advance()
                BooleanLiteral(true, span)
            }
            TokenType.FALSE -> {
                advance()
                BooleanLiteral(false, span)
            }
            TokenType.IDENTIFIER -> {
                val name = currentToken!!.lexeme
                advance()
                Variable(name, span)
            }
            TokenType.LPAREN -> {
                advance()
                val expr = expression()
                expect(TokenType.RPAREN)
                expr
            }
            else -> throw ParseException("Unexpected token: ${currentToken?.type}", span)
        }
    }

    private fun expect(vararg types: TokenType): Token {
        val token = currentToken ?: throw ParseException("Unexpected end of input", Span(Position(1, 1), Position(1, 1)))
        if (token.type !in types) {
            throw ParseException("Expected ${types.joinToString(", ")} but got ${token.type}", token.span)
        }
        advance()
        return token
    }

    private fun advance() {
        currentToken = nextToken
        nextToken = if (tokens.hasNext()) tokens.next() else null
        if (currentToken == null && nextToken == null) {
            currentToken = Token(TokenType.EOF, "", Span(Position(1, 1), Position(1, 1)))
        }
    }

    private fun saveState(): ParserState = ParserState(currentToken, nextToken)

    private fun restoreState(state: ParserState) {
        currentToken = state.currentToken
        nextToken = state.nextToken
    }

    private data class ParserState(
        val currentToken: Token?,
        val nextToken: Token?,
    )
}

class ParseException(
    val rawMessage: String,
    val span: Span,
) : Exception(rawMessage)
