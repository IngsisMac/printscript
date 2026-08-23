package com.printscript.token

import com.printscript.common.Position
import com.printscript.common.Span

object TokenFactory {
    private val DEFAULT_SPAN = Span(Position(1, 1), Position(1, 1))

    private val CANONICAL_LEXEMES: Map<TokenType, String> =
        mapOf(
            TokenType.LET to "let",
            TokenType.CONST to "const",
            TokenType.NUMBER to "number",
            TokenType.STRING to "string",
            TokenType.BOOLEAN to "boolean",
            TokenType.IF to "if",
            TokenType.ELSE to "else",
            TokenType.PRINTLN to "println",
            TokenType.READ_INPUT to "readInput",
            TokenType.READ_ENV to "readEnv",
            TokenType.TRUE to "true",
            TokenType.FALSE to "false",
            TokenType.PLUS to "+",
            TokenType.MINUS to "-",
            TokenType.STAR to "*",
            TokenType.SLASH to "/",
            TokenType.EQUAL to "=",
            TokenType.LPAREN to "(",
            TokenType.RPAREN to ")",
            TokenType.LBRACE to "{",
            TokenType.RBRACE to "}",
            TokenType.SEMICOLON to ";",
            TokenType.COMMA to ",",
            TokenType.COLON to ":",
            TokenType.EOF to "",
        )

    fun create(
        type: TokenType,
        lexeme: String? = null,
        span: Span = DEFAULT_SPAN,
    ): Token {
        val resolvedLexeme = lexeme ?: CANONICAL_LEXEMES[type] ?: type.name.lowercase()
        return Token(type, resolvedLexeme, span)
    }

    fun tokens(vararg items: Any): List<Token> =
        items.map { item ->
            when (item) {
                is Token -> item
                is TokenType -> create(item)
                is Pair<*, *> -> {
                    val first =
                        item.first as? TokenType
                            ?: error("Expected TokenType as first element of Pair, got: ${item.first}")
                    val second = item.second?.toString() ?: ""
                    create(first, second)
                }
                else -> error("Unsupported item type in TokenFactory.tokens: $item")
            }
        }

    fun declaration(
        name: String,
        type: String,
        value: String? = null,
        isConst: Boolean = false,
        span: Span = DEFAULT_SPAN,
    ): List<Token> {
        val list = buildDeclarationHeader(name, type, isConst, span)
        if (value != null) {
            list.add(create(TokenType.EQUAL, span = span))
            val (valType, valLexeme) = resolveValueToken(type, value)
            list.add(create(valType, valLexeme, span))
        }
        list.add(create(TokenType.SEMICOLON, span = span))
        return list
    }

    private fun buildDeclarationHeader(
        name: String,
        type: String,
        isConst: Boolean,
        span: Span,
    ): MutableList<Token> {
        val keywordType = if (isConst) TokenType.CONST else TokenType.LET
        val typeEnum = resolveTypeToken(type)
        return mutableListOf(
            create(keywordType, span = span),
            create(TokenType.IDENTIFIER, name, span),
            create(TokenType.COLON, span = span),
            create(typeEnum, type, span),
        )
    }

    private fun resolveTypeToken(type: String): TokenType =
        when (type.lowercase()) {
            "number" -> TokenType.NUMBER
            "string" -> TokenType.STRING
            "boolean" -> TokenType.BOOLEAN
            else -> TokenType.IDENTIFIER
        }

    private fun resolveValueToken(
        type: String,
        value: String,
    ): Pair<TokenType, String> =
        when {
            type == "number" || value.toDoubleOrNull() != null -> TokenType.NUMBER_LITERAL to value
            type == "boolean" && (value == "true" || value == "false") ->
                if (value == "true") TokenType.TRUE to "true" else TokenType.FALSE to "false"
            else -> TokenType.STRING_LITERAL to value
        }
}
