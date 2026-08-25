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
import java.io.Writer

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

    @Test
    @DisplayName("Extensibilidad: Formatter soporta reglas personalizadas inyectadas sin modificar DefaultFormatter")
    fun formatterSoportaReglasPersonalizadas() {
        val customRule =
            object : com.printscript.formatter.rule.FormattingRule {
                override fun appliesTo(statement: com.printscript.ast.Statement): Boolean = statement is Declaration

                override fun format(
                    statement: com.printscript.ast.Statement,
                    writer: Writer,
                    context: FormatterContext,
                ) {
                    val decl = statement as Declaration
                    writer.write("// CUSTOM: ${decl.name}")
                }
            }

        val customFormatter = DefaultFormatter(rules = listOf(customRule))
        val decl = Declaration("x", "number", null, dummySpan, isConst = false)
        val writer = StringWriter()

        customFormatter.format(decl, writer)

        assertEquals("// CUSTOM: x", writer.toString())
    }

    @Test
    @DisplayName("Error al formatear sentencia sin regla compatible")
    fun errorAlFormatearSentenciaSinRegla() {
        val emptyFormatter = DefaultFormatter(rules = emptyList())
        val decl = Declaration("x", "number", null, dummySpan, isConst = false)
        val writer = StringWriter()

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            emptyFormatter.format(decl, writer)
        }
    }

    @Test
    @DisplayName("Extensibilidad: ExpressionFormatter soporta reglas personalizadas de expresión")
    fun expressionFormatterSoportaReglasPersonalizadas() {
        val customExprRule =
            object : com.printscript.formatter.expression.ExpressionRule {
                override fun appliesTo(expression: com.printscript.ast.Expression): Boolean =
                    expression is NumberLiteral

                override fun format(
                    expression: com.printscript.ast.Expression,
                    config: FormatterConfig,
                    formatter: ExpressionFormatter,
                ): String = "#NUM#${(expression as NumberLiteral).value}"
            }

        val customExprFormatter = DefaultExpressionFormatter(rules = listOf(customExprRule))
        val result = customExprFormatter.format(NumberLiteral("123", dummySpan), FormatterConfig())

        assertEquals("#NUM#123", result)
    }

    @Test
    @DisplayName("Error al formatear expresión sin regla compatible")
    fun errorAlFormatearExpresionSinRegla() {
        val emptyExprFormatter = DefaultExpressionFormatter(rules = emptyList())

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            emptyExprFormatter.format(NumberLiteral("123", dummySpan), FormatterConfig())
        }
    }
}
