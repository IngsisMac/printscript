package com.printscript.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SpanTest {
    private lateinit var startPos: Position
    private lateinit var endPosSameLine: Position
    private lateinit var endPosNextLine: Position

    @BeforeEach
    fun setUp() {
        startPos = Position(1, 1)
        endPosSameLine = Position(1, 10)
        endPosNextLine = Position(2, 5)
    }

    @Test
    @DisplayName("Instanciación válida de Span cuando start precede o iguala a end en la misma línea")
    fun instanciacionValidaDeSpanCuandoStartPrecedeOIgualaAEndEnMismaLinea() {
        val start = startPos
        val end = endPosSameLine

        val span = Span(start, end)

        assertEquals(start, span.start)
        assertEquals(end, span.end)
    }

    @Test
    @DisplayName("Instanciación válida de Span cuando start.line es menor a end.line")
    fun instanciacionValidaDeSpanCuandoStartLineEsMenorAEndLine() {
        val start = startPos
        val end = endPosNextLine

        val span = Span(start, end)

        assertEquals(start, span.start)
        assertEquals(end, span.end)
    }

    @Test
    @DisplayName("Span rechaza start posterior a end en la misma línea lanzando IllegalArgumentException")
    fun spanRechazaStartPosteriorAEndEnMismaLineaLanzandoIllegalArgumentException() {
        val start = Position(1, 10)
        val end = Position(1, 5)

        val exception = assertThrows<IllegalArgumentException> {
            Span(start, end)
        }

        assertEquals("Invalid span: start [1:10] must be before end [1:5]", exception.message)
    }

    @Test
    @DisplayName("Span rechaza start.line mayor a end.line lanzando IllegalArgumentException")
    fun spanRechazaStartLineMayorAEndLineLanzandoIllegalArgumentException() {
        val start = Position(2, 1)
        val end = Position(1, 10)

        val exception = assertThrows<IllegalArgumentException> {
            Span(start, end)
        }

        assertEquals("Invalid span: start [2:1] must be before end [1:10]", exception.message)
    }

    @Test
    @DisplayName("Span renderiza su representación toString como start dash end")
    fun spanRenderizaSuRepresentacionToStringComoStartDashEnd() {
        val span = Span(startPos, endPosSameLine)

        val result = span.toString()

        assertEquals("[1:1]-[1:10]", result)
    }

    @Test
    @DisplayName("Igualdad estructural e inmutabilidad de Span")
    fun igualdadEstructuralEInmutabilidadDeSpan() {
        val span1 = Span(startPos, endPosSameLine)
        val span2 = Span(startPos, endPosSameLine)
        val span3 = Span(startPos, endPosNextLine)

        assertEquals(span1, span2)
        assertEquals(span1.hashCode(), span2.hashCode())
        assertNotEquals(span1, span3)
    }
}
