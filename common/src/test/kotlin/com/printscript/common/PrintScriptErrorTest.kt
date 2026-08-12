package com.printscript.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PrintScriptErrorTest {
    private lateinit var defaultSpan: Span
    private lateinit var defaultErrorMessage: String

    @BeforeEach
    fun setUp() {
        defaultSpan = Span(Position(1, 1), Position(1, 10))
        defaultErrorMessage = "Unclosed string literal"
    }

    @Test
    @DisplayName("PrintScriptError encapsula el mensaje y span renderizando la salida formateada")
    fun printScriptErrorEncapsulaMensajeYSpanRenderizandoSalidaFormateada() {
        val span = defaultSpan
        val message = defaultErrorMessage

        val error = PrintScriptError(message, span)

        assertEquals("[1:1]-[1:10] Error: Unclosed string literal", error.render())
    }

    @Test
    @DisplayName("PrintScriptError toString equivale exactamente al resultado de render")
    fun printScriptErrorToStringEquivaleExactamenteAlResultadoDeRender() {
        val span = defaultSpan
        val message = defaultErrorMessage
        val error = PrintScriptError(message, span)

        val toStringResult = error.toString()

        assertEquals(error.render(), toStringResult)
    }

    @Test
    @DisplayName("Igualdad estructural e inmutabilidad de PrintScriptError")
    fun igualdadEstructuralEInmutabilidadDePrintScriptError() {
        val error1 = PrintScriptError(defaultErrorMessage, defaultSpan)
        val error2 = PrintScriptError(defaultErrorMessage, defaultSpan)
        val error3 = PrintScriptError("Another error", defaultSpan)

        assertEquals(error1, error2)
        assertEquals(error1.hashCode(), error2.hashCode())
        assertNotEquals(error1, error3)
    }
}
