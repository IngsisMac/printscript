package com.printscript.formatter

import com.printscript.ast.Assignment
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

class DefaultFormatterTest {
    private lateinit var dummySpan: Span
    private lateinit var formatter: DefaultFormatter
    private lateinit var writer: StringWriter
    private lateinit var defaultConfig: FormatterConfig

    @BeforeEach
    fun setUp() {
        dummySpan = Span(Position(1, 1), Position(1, 10))
        formatter = DefaultFormatter()
        writer = StringWriter()
        defaultConfig = FormatterConfig()
    }

    @Test
    @DisplayName("Formateo de declaración de variable let con valor inicial")
    fun formateoDeDeclaracionLetConValor() {
        val decl = Declaration("a", "number", NumberLiteral("5", dummySpan), dummySpan, isConst = false)

        formatter.format(decl, writer, defaultConfig)

        assertEquals("let a: number = 5;\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de declaración de constante con valor inicial")
    fun formateoDeDeclaracionConstConValor() {
        val decl = Declaration("b", "string", StringLiteral("hola", dummySpan), dummySpan, isConst = true)

        formatter.format(decl, writer, defaultConfig)

        assertEquals("const b: string = \"hola\";\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de declaración de variable sin inicialización")
    fun formateoDeDeclaracionSinValor() {
        val decl = Declaration("x", "number", null, dummySpan, isConst = false)

        formatter.format(decl, writer, defaultConfig)

        assertEquals("let x: number;\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de declaración con opciones de espaciado en dos puntos")
    fun formateoDeDeclaracionConEspaciadoEnDosPuntos() {
        val config =
            FormatterConfig(
                enforceSpacingBeforeColonInDeclaration = true,
                enforceSpacingAfterColonInDeclaration = false,
            )
        val decl = Declaration("a", "number", NumberLiteral("10", dummySpan), dummySpan, isConst = false)

        formatter.format(decl, writer, config)

        assertEquals("let a :number = 10;\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de asignación de variable con y sin espacios alrededor del igual")
    fun formateoDeAsignacion() {
        val configNoSpace = FormatterConfig(enforceSpacingAroundEquals = false)
        val assignment = Assignment("x", NumberLiteral("20", dummySpan), dummySpan)

        formatter.format(assignment, writer, configNoSpace)

        assertEquals("x=20;\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia println con saltos de línea adicionales configurados")
    fun formateoDePrintStatementConSaltosDeLineaAdicionales() {
        val config = FormatterConfig(lineBreaksAfterPrintln = 2)
        val printStmt = PrintStatement(StringLiteral("Test", dummySpan), dummySpan)

        formatter.format(printStmt, writer, config)

        assertEquals("println(\"Test\");\n\n", writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia if con llaves en la misma línea e indentación personalizada")
    fun formateoDeIfConLlavesMismaLinea() {
        val config = FormatterConfig(ifBraceSameLine = true, indentInsideIf = 2)
        val insideDecl = Declaration("inside", "number", NumberLiteral("1", dummySpan), dummySpan, isConst = false)
        val ifStmt = IfStatement(Variable("cond", dummySpan), listOf(insideDecl), null, dummySpan)

        formatter.format(ifStmt, writer, config)

        val expected =
            """
            if (cond) {
              let inside: number = 1;
            }
            """.trimIndent() + "\n"

        assertEquals(expected, writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia if con llaves en línea inferior")
    fun formateoDeIfConLlavesEnLineaInferior() {
        val config = FormatterConfig(ifBraceBelowLine = true, indentInsideIf = 4)
        val insideDecl = Declaration("val", "boolean", BooleanLiteral(true, dummySpan), dummySpan, isConst = false)
        val ifStmt = IfStatement(Variable("cond", dummySpan), listOf(insideDecl), null, dummySpan)

        formatter.format(ifStmt, writer, config)

        val expected =
            """
            if (cond)
            {
                let val: boolean = true;
            }
            """.trimIndent() + "\n"

        assertEquals(expected, writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia if-else completa con llaves en la misma línea")
    fun formateoDeIfElseCompleto() {
        val thenPrint = PrintStatement(StringLiteral("Verdadero", dummySpan), dummySpan)
        val elsePrint = PrintStatement(StringLiteral("Falso", dummySpan), dummySpan)
        val ifStmt = IfStatement(Variable("isValid", dummySpan), listOf(thenPrint), listOf(elsePrint), dummySpan)

        formatter.format(ifStmt, writer, defaultConfig)

        val expected =
            """
            if (isValid) {
                println("Verdadero");
            } else {
                println("Falso");
            }
            """.trimIndent() + "\n"

        assertEquals(expected, writer.toString())
    }

    @Test
    @DisplayName("Formateo de sentencia if-else completa con llaves en línea inferior")
    fun formateoDeIfElseConLlavesEnLineaInferior() {
        val config = FormatterConfig(ifBraceBelowLine = true, indentInsideIf = 4)
        val thenPrint = PrintStatement(StringLiteral("Verdadero", dummySpan), dummySpan)
        val elsePrint = PrintStatement(StringLiteral("Falso", dummySpan), dummySpan)
        val ifStmt = IfStatement(Variable("isValid", dummySpan), listOf(thenPrint), listOf(elsePrint), dummySpan)

        formatter.format(ifStmt, writer, config)

        val expected =
            """
            if (isValid)
            {
                println("Verdadero");
            }
            else
            {
                println("Falso");
            }
            """.trimIndent() + "\n"

        assertEquals(expected, writer.toString())
    }
}
