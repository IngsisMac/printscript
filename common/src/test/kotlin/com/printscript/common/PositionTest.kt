package com.printscript.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PositionTest {
    private var defaultLine: Int = 0
    private var defaultColumn: Int = 0

    @BeforeEach
    fun setUp() {
        defaultLine = 1
        defaultColumn = 5
    }

    @Test
    @DisplayName("Instanciación válida de Position con línea y columna mayores o iguales a 1")
    fun instanciacionValidaDePositionConLineaYColumnaMayorOIgualAUno() {
        val line = defaultLine
        val column = defaultColumn

        val position = Position(line, column)

        assertEquals(1, position.line)
        assertEquals(5, position.column)
    }

    @Test
    @DisplayName("Position rechaza línea menor a 1 lanzando IllegalArgumentException")
    fun positionRechazaLineaMenorAUnoLanzandoIllegalArgumentException() {
        val invalidLine = 0
        val column = defaultColumn

        val exception = assertThrows<IllegalArgumentException> {
            Position(invalidLine, column)
        }

        assertEquals("Line must be >= 1, got 0", exception.message)
    }

    @Test
    @DisplayName("Position rechaza columna menor a 1 lanzando IllegalArgumentException")
    fun positionRechazaColumnaMenorAUnoLanzandoIllegalArgumentException() {
        val line = defaultLine
        val invalidColumn = 0

        val exception = assertThrows<IllegalArgumentException> {
            Position(line, invalidColumn)
        }

        assertEquals("Column must be >= 1, got 0", exception.message)
    }

    @Test
    @DisplayName("Position renderiza su representación toString como línea y columna")
    fun positionRenderizaSuRepresentacionToStringComoLineaYColumna() {
        val position = Position(defaultLine, defaultColumn)

        val result = position.toString()

        assertEquals("[1:5]", result)
    }

    @Test
    @DisplayName("Igualdad estructural e inmutabilidad de Position")
    fun igualdadEstructuralEInmutabilidadDePosition() {
        val pos1 = Position(defaultLine, defaultColumn)
        val pos2 = Position(defaultLine, defaultColumn)
        val pos3 = Position(2, defaultColumn)

        assertEquals(pos1, pos2)
        assertEquals(pos1.hashCode(), pos2.hashCode())
        assertNotEquals(pos1, pos3)
    }
}
