package com.printscript.token

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TokenFactoryTest {
    private lateinit var factory: TokenFactory

    @BeforeEach
    fun setUp() {
        factory = TokenFactory
    }

    @Test
    @DisplayName("Creación de token con lexema canónico por defecto")
    fun creacionDeTokenConLexemaCanonico() {
        val letToken = factory.create(TokenType.LET)
        val colonToken = factory.create(TokenType.COLON)
        val semicolonToken = factory.create(TokenType.SEMICOLON)

        assertEquals("let", letToken.lexeme)
        assertEquals(":", colonToken.lexeme)
        assertEquals(";", semicolonToken.lexeme)
    }

    @Test
    @DisplayName("Creación de token con lexema personalizado")
    fun creacionDeTokenConLexemaPersonalizado() {
        val idToken = factory.create(TokenType.IDENTIFIER, "myVar")

        assertEquals(TokenType.IDENTIFIER, idToken.type)
        assertEquals("myVar", idToken.lexeme)
    }

    @Test
    @DisplayName("Construcción de secuencia combinada de tokens a partir de varargs")
    fun construccionDeSecuenciaCombinadaDeTokens() {
        val tokens =
            factory.tokens(
                TokenType.LET,
                TokenType.IDENTIFIER to "x",
                TokenType.COLON,
                TokenType.NUMBER,
                TokenType.EQUAL,
                TokenType.NUMBER_LITERAL to "42",
                TokenType.SEMICOLON,
            )

        assertEquals(7, tokens.size)
        assertEquals(TokenType.LET, tokens[0].type)
        assertEquals("x", tokens[1].lexeme)
        assertEquals("42", tokens[5].lexeme)
        assertEquals(TokenType.SEMICOLON, tokens[6].type)
    }

    @Test
    @DisplayName("Helper de declaración genera secuencia completa con inicialización")
    fun helperDeDeclaracionGeneraSecuenciaCompleta() {
        val tokens = factory.declaration("contador", "number", "10", isConst = false)

        assertEquals(7, tokens.size)
        assertEquals(TokenType.LET, tokens[0].type)
        assertEquals("contador", tokens[1].lexeme)
        assertEquals(":", tokens[2].lexeme)
        assertEquals("number", tokens[3].lexeme)
        assertEquals("=", tokens[4].lexeme)
        assertEquals("10", tokens[5].lexeme)
        assertEquals(";", tokens[6].lexeme)
    }

    @Test
    @DisplayName("Helper de declaración constante sin inicialización")
    fun helperDeDeclaracionConstanteSinInicializacion() {
        val tokens = factory.declaration("activo", "boolean", isConst = true)

        assertEquals(5, tokens.size)
        assertEquals(TokenType.CONST, tokens[0].type)
        assertEquals("activo", tokens[1].lexeme)
        assertEquals("boolean", tokens[3].lexeme)
        assertEquals(";", tokens[4].lexeme)
    }
}
