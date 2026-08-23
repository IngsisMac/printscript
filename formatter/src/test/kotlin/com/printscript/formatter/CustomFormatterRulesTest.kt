package com.printscript.formatter

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

class CustomFormatterRulesTest {
    private lateinit var formatter: DefaultFormatter
    private lateinit var dummySpan: Span

    @BeforeEach
    fun setUp() {
        formatter = DefaultFormatter()
        dummySpan = Span(Position(1, 1), Position(1, 10))
    }

    @Test
    @DisplayName("Formateo de declaración sin espacio antes de punto y coma")
    fun formateoDeclaracionSinEspacioAntesDePuntoYComa() {
        val decl =
            Declaration(
                name = "x",
                type = "number",
                value = NumberLiteral("42", dummySpan),
                span = dummySpan,
            )
        val config = FormatterConfig(enforceNoSpaceBeforeSemicolon = true)
        val writer = StringWriter()

        formatter.format(decl, writer, config)

        assertEquals("let x: number = 42;", writer.toString())
    }

    @Test
    @DisplayName("Formateo de asignación con espacio antes de punto y coma cuando regla está desactivada")
    fun formateoAsignacionConEspacioAntesDePuntoYComa() {
        val assignment =
            Assignment(
                name = "x",
                value = NumberLiteral("100", dummySpan),
                span = dummySpan,
            )
        val config = FormatterConfig(enforceNoSpaceBeforeSemicolon = false)
        val writer = StringWriter()

        formatter.format(assignment, writer, config)

        assertEquals("x = 100 ;", writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia println básica")
    fun formateoPrintlnBasica() {
        val printStmt =
            PrintStatement(
                expression = StringLiteral("Hello", dummySpan),
                span = dummySpan,
            )
        val config = FormatterConfig()
        val writer = StringWriter()

        formatter.format(printStmt, writer, config)

        assertEquals("println(\"Hello\");", writer.toString())
    }
}
