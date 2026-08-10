package com.printscript.lexer

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import java.io.Reader

class Lexer(
    private val source: Reader,
    private val version: Version,
) : Iterator<Token> {
    private var currentLine = 1
    private var currentColumn = 1
    private var currentChar: Int? = source.read()
    private var nextToken: Token? = null
    private val keywords = mapOf(
        "let" to TokenType.LET,
        "const" to TokenType.CONST,
        "number" to TokenType.NUMBER,
        "string" to TokenType.STRING,
        "boolean" to TokenType.BOOLEAN,
        "println" to TokenType.PRINTLN,
        "readInput" to TokenType.READ_INPUT,
        "readEnv" to TokenType.READ_ENV,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
    )

    override fun hasNext(): Boolean {
        if (nextToken != null) return true
        if (currentChar == null) return false
        nextToken = readNextToken()
        return nextToken != null
    }

    override fun next(): Token {
        if (hasNext()) {
            val token = nextToken!!
            nextToken = null
            return token
        }
        throw NoSuchElementException("No more tokens")
    }

    private fun readNextToken(): Token? {
        skipWhitespaceExceptNewline()

        if (currentChar == null) return null
        if (currentChar == '\n'.code) {
            advance()
            currentLine++
            currentColumn = 1
            return readNextToken()
        }

        val startLine = currentLine
        val startColumn = currentColumn
        val startPos = Position(startLine, startColumn)

        return when {
            currentChar == '('.code -> {
                advance()
                Token(TokenType.LPAREN, "(", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == ')'.code -> {
                advance()
                Token(TokenType.RPAREN, ")", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '{'.code -> {
                advance()
                Token(TokenType.LBRACE, "{", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '}'.code -> {
                advance()
                Token(TokenType.RBRACE, "}", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == ';'.code -> {
                advance()
                Token(TokenType.SEMICOLON, ";", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == ','.code -> {
                advance()
                Token(TokenType.COMMA, ",", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == ':'.code -> {
                advance()
                Token(TokenType.COLON, ":", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '+'.code -> {
                advance()
                Token(TokenType.PLUS, "+", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '-'.code -> {
                advance()
                Token(TokenType.MINUS, "-", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '*'.code -> {
                advance()
                Token(TokenType.STAR, "*", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '/'.code -> {
                advance()
                Token(TokenType.SLASH, "/", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '='.code -> {
                advance()
                Token(TokenType.EQUAL, "=", Span(startPos, Position(currentLine, currentColumn - 1)))
            }
            currentChar == '"'.code -> readStringLiteral(startPos)
            currentChar?.let { it.toChar().isDigit() } == true -> readNumberLiteral(startPos)
            currentChar?.let { it.toChar().isLetter() || it.toChar() == '_' } == true -> readIdentifierOrKeyword(startPos)
            else -> {
                advance()
                readNextToken() // Skip unknown chars
            }
        }
    }

    private fun readStringLiteral(startPos: Position): Token {
        val sb = StringBuilder()
        advance() // skip opening quote

        while (currentChar != null && currentChar != '"'.code) {
            sb.append(currentChar!!.toChar())
            if (currentChar == '\n'.code) {
                currentLine++
                currentColumn = 0
            }
            advance()
        }

        if (currentChar == '"'.code) advance() // skip closing quote

        val lexeme = "\"" + sb.toString() + "\""
        return Token(TokenType.STRING_LITERAL, sb.toString(), Span(startPos, Position(currentLine, currentColumn)))
    }

    private fun readNumberLiteral(startPos: Position): Token {
        val sb = StringBuilder()

        while (currentChar != null && (currentChar!!.toChar().isDigit() || currentChar == '.'.code)) {
            sb.append(currentChar!!.toChar())
            advance()
        }

        return Token(TokenType.NUMBER_LITERAL, sb.toString(), Span(startPos, Position(currentLine, currentColumn - 1)))
    }

    private fun readIdentifierOrKeyword(startPos: Position): Token {
        val sb = StringBuilder()

        while (currentChar != null && (currentChar!!.toChar().isLetterOrDigit() || currentChar == '_'.code)) {
            sb.append(currentChar!!.toChar())
            advance()
        }

        val lexeme = sb.toString()
        val type = keywords[lexeme] ?: TokenType.IDENTIFIER

        return Token(type, lexeme, Span(startPos, Position(currentLine, currentColumn - 1)))
    }

    private fun skipWhitespaceExceptNewline() {
        while (currentChar != null && currentChar!!.toChar() in " \t\r") {
            advance()
        }
    }

    private fun advance() {
        currentChar = source.read()
        currentColumn++
        if (currentChar == -1) {
            currentChar = null
        }
    }
}
