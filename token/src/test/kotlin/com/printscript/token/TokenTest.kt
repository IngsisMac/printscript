package com.printscript.token

import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TokenTest {
    private lateinit var defaultSpan: Span
    private lateinit var letToken: Token
    private lateinit var identifierToken: Token

    @BeforeEach
    fun setUp() {
        defaultSpan = Span(Position(1, 1), Position(1, 4))
        letToken = Token(TokenType.LET, "let", defaultSpan)
        identifierToken = Token(TokenType.IDENTIFIER, "myVar", defaultSpan)
    }

    @Test
    @DisplayName("Token conserva su tipo, lexema y span correctamente")
    fun tokenConservaTipoLexemaYSpan() {
        val type = letToken.type
        val lexeme = letToken.lexeme
        val span = letToken.span

        assertEquals(TokenType.LET, type)
        assertEquals("let", lexeme)
        assertEquals(defaultSpan, span)
    }

    @Test
    @DisplayName("Instancias de Token con mismos atributos son iguales por ser data class")
    fun tokensIgualesTienenMismoEqualsYHashCode() {
        val duplicateLet = Token(TokenType.LET, "let", defaultSpan)

        assertEquals(letToken, duplicateLet)
        assertEquals(letToken.hashCode(), duplicateLet.hashCode())
        assertNotEquals(letToken, identifierToken)
    }

    @Test
    @DisplayName("Copia de Token permite modificar atributos específicos preservando los demás")
    fun tokenCopyPermiteModificarPropiedades() {
        val newSpan = Span(Position(2, 5), Position(2, 10))

        val copiedToken = letToken.copy(span = newSpan)

        assertEquals(TokenType.LET, copiedToken.type)
        assertEquals("let", copiedToken.lexeme)
        assertEquals(newSpan, copiedToken.span)
    }

    @Test
    @DisplayName("Representación en texto de Token incluye todos sus componentes")
    fun tokenToStringContieneInformacionCompleta() {
        val tokenString = identifierToken.toString()

        assertEquals("Token(type=IDENTIFIER, lexeme=myVar, span=$defaultSpan)", tokenString)
    }
}
