package com.printscript.token

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TokenTypeTest {
    private lateinit var allTokenTypes: Array<TokenType>

    @BeforeEach
    fun setUp() {
        allTokenTypes = TokenType.entries.toTypedArray()
    }

    @Test
    @DisplayName("TokenType contiene todas las palabras clave requeridas por PrintScript 1.0 y 1.1")
    fun contieneTodasLasPalabrasClave() {
        val keywords =
            listOf(
                TokenType.LET,
                TokenType.CONST,
                TokenType.NUMBER,
                TokenType.STRING,
                TokenType.BOOLEAN,
                TokenType.IF,
                TokenType.ELSE,
                TokenType.PRINTLN,
                TokenType.READ_INPUT,
                TokenType.READ_ENV,
            )

        keywords.forEach { keyword ->
            assertTrue(allTokenTypes.contains(keyword))
        }
    }

    @Test
    @DisplayName("TokenType contiene todos los operadores y delimitadores del lenguaje")
    fun contieneOperadoresYDelimitadores() {
        val operatorsAndDelimiters =
            listOf(
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.STAR,
                TokenType.SLASH,
                TokenType.EQUAL,
                TokenType.LPAREN,
                TokenType.RPAREN,
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.SEMICOLON,
                TokenType.COMMA,
                TokenType.COLON,
            )

        operatorsAndDelimiters.forEach { symbol ->
            assertTrue(allTokenTypes.contains(symbol))
        }
    }

    @Test
    @DisplayName("TokenType contiene los literales e identificadores necesarios")
    fun contieneLiteralesEIdentificadores() {
        val literals =
            listOf(
                TokenType.IDENTIFIER,
                TokenType.NUMBER_LITERAL,
                TokenType.STRING_LITERAL,
                TokenType.TRUE,
                TokenType.FALSE,
                TokenType.EOF,
            )

        literals.forEach { literal ->
            assertTrue(allTokenTypes.contains(literal))
        }
    }

    @Test
    @DisplayName("valueOf retorna el TokenType correspondiente según el nombre exacto de la constante")
    fun valueOfRetornaConstanteCorrecta() {
        val letType = TokenType.valueOf("LET")
        val constType = TokenType.valueOf("CONST")
        val eofType = TokenType.valueOf("EOF")

        assertEquals(TokenType.LET, letType)
        assertEquals(TokenType.CONST, constType)
        assertEquals(TokenType.EOF, eofType)
    }

    @Test
    @DisplayName("Cantidad total de elementos en TokenType coincide con los definidos en el enum")
    fun cantidadTotalDeElementosEsCorrecta() {
        val count = allTokenTypes.size

        assertEquals(28, count)
    }
}
