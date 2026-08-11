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
    private val config: TokenConfig = TokenConfig.from(version),
) : Iterator<Token> {
    private var currentLine = 1
    private var currentColumn = 1
    private var currentChar: Int? = source.read()
    private var nextToken: Token? = null

    init {
        if (currentChar == -1) {
            currentChar = null
        }
    }

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
        skipWhitespace()

        if (currentChar == null) return null

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
            currentChar == '"'.code || currentChar == '\''.code -> readStringLiteral(startPos)
            currentChar?.let { it.toChar().isDigit() } == true -> readNumberLiteral(startPos)
            currentChar?.let { it.toChar().isLetter() || it.toChar() == '_' } == true -> readIdentifierOrKeyword(startPos)
            else -> {
                val badChar = currentChar!!.toChar()
                val errPos = Position(currentLine, currentColumn)
                advance()
                throw LexerException("Unexpected character '$badChar'", Span(startPos, errPos))
            }
        }
    }

    private fun readStringLiteral(startPos: Position): Token {
        val quoteChar = currentChar!!.toChar()
        val sb = StringBuilder()
        advance() // skip opening quote

        var isClosed = false

        while (currentChar != null) {
            val ch = currentChar!!.toChar()
            if (ch == quoteChar) {
                isClosed = true
                advance() // skip closing quote
                break
            }
            sb.append(ch)
            advance()
        }

        if (!isClosed) {
            val errPos = Position(currentLine, currentColumn)
            throw LexerException("Unterminated string literal", Span(startPos, errPos))
        }

        val endPos = Position(currentLine, currentColumn - 1)
        return Token(TokenType.STRING_LITERAL, sb.toString(), Span(startPos, endPos))
    }

    private fun readNumberLiteral(startPos: Position): Token {
        val sb = StringBuilder()
        var hasDecimalPoint = false

        while (currentChar != null) {
            val ch = currentChar!!.toChar()
            if (ch.isDigit()) {
                sb.append(ch)
                advance()
            } else if (ch == '.') {
                if (hasDecimalPoint) {
                    val errPos = Position(currentLine, currentColumn)
                    throw LexerException(
                        "Invalid number literal: multiple decimal points in '${sb.toString()}.'",
                        Span(startPos, errPos),
                    )
                }
                hasDecimalPoint = true
                sb.append(ch)
                advance()
            } else {
                break
            }
        }

        val lexeme = sb.toString()
        if (lexeme.endsWith(".")) {
            val errPos = Position(currentLine, currentColumn - 1)
            throw LexerException("Invalid number literal: trailing decimal point in '$lexeme'", Span(startPos, errPos))
        }

        val endPos = Position(currentLine, currentColumn - 1)
        return Token(TokenType.NUMBER_LITERAL, lexeme, Span(startPos, endPos))
    }

    private fun readIdentifierOrKeyword(startPos: Position): Token {
        val sb = StringBuilder()

        while (currentChar != null && (currentChar!!.toChar().isLetterOrDigit() || currentChar == '_'.code)) {
            sb.append(currentChar!!.toChar())
            advance()
        }

        val lexeme = sb.toString()
        val type = config.keywords[lexeme] ?: TokenType.IDENTIFIER

        val endPos = Position(currentLine, currentColumn - 1)
        return Token(type, lexeme, Span(startPos, endPos))
    }

    private fun skipWhitespace() {
        while (currentChar != null) {
            when (currentChar!!.toChar()) {
                ' ', '\t', '\r' -> advance()
                '\n' -> {
                    advance()
                    currentLine++
                    currentColumn = 1
                }
                else -> break
            }
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

