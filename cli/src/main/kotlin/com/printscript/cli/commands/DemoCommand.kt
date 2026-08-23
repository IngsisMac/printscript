package com.printscript.cli.commands

import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import com.printscript.runner.PrintScriptRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Callable

@Command(
    name = "demo",
    aliases = ["e2e", "showcase"],
    description = ["Ejecuta una demostración de punta a punta (E2E) con toda la implementación real de PrintScript."],
    mixinStandardHelpOptions = true,
)
class DemoCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    override fun call(): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        printBanner(out)
        demoPrintScript10(out)
        demoPrintScript11(out)
        demoLinter(out)
        demoFormatter(out)
        demoErrorHandling(out)
        out.println("================================================================================")
        out.println("          DEMOSTRACIÓN E2E DE PRINTSCRIPT FINALIZADA CON ÉXITO")
        out.println("================================================================================")
        return 0
    }

    private fun printBanner(out: PrintWriter) {
        out.println("================================================================================")
        out.println("                   PRINTSCRIPT - DEMOSTRACIÓN END-TO-END (E2E)                  ")
        out.println("             Pipeline real: Lexer -> Parser -> Interpreter / Formatter / Linter  ")
        out.println("================================================================================")
    }

    private fun demoPrintScript10(out: PrintWriter) {
        out.println("\n[1/5] Ejecución real de PrintScript 1.0 (Declaraciones, Aritmética y Strings)")
        out.println("--------------------------------------------------------------------------------")
        val source = "let a: number = 12;\nlet b: number = 4;\nlet c: number = a / b;\nprintln(\"Resultado: \" + c);"
        out.println("Código fuente:")
        out.println(source)
        out.println("\nSalida del Intérprete:")
        val emitter = OutputEmitter { line -> out.println("  > $line") }
        val result = PrintScriptRunner.execute(source.reader(), Version.V1_0, emitter, InputSource { "" })
        if (result.errors.isNotEmpty()) {
            result.errors.forEach { out.println("  [ERROR] ${it.render()}") }
        }
    }

    private fun demoPrintScript11(out: PrintWriter) {
        out.println("\n[2/5] Ejecución real de PrintScript 1.1 (Constantes, Booleans, If-Else y Env)")
        out.println("--------------------------------------------------------------------------------")
        val source =
            "const max: number = 100;\nlet isOk: boolean = true;\nif (isOk) {\n" +
                "    println(\"Score maximo: \" + max);\n} else {\n    println(\"Fallo\");\n}\n" +
                "let user: string = readEnv(\"USER\");\nprintln(\"Usuario activo: \" + user);"
        out.println("Código fuente:")
        out.println(source)
        out.println("\nSalida del Intérprete:")
        val emitter = OutputEmitter { line -> out.println("  > $line") }
        val envSource = EnvSource { key -> if (key == "USER") "ingsis_demo_user" else System.getenv(key) }
        val result = PrintScriptRunner.execute(source.reader(), Version.V1_1, emitter, InputSource { "" }, envSource)
        if (result.errors.isNotEmpty()) {
            result.errors.forEach { out.println("  [ERROR] ${it.render()}") }
        }
    }

    private fun demoLinter(out: PrintWriter) {
        out.println("\n[3/5] Análisis estático real (Linter con reporte de errores y posiciones)")
        out.println("--------------------------------------------------------------------------------")
        val source = "let invalid_snake_case: number = 10;\nprintln(\"Expresion: \" + (5 + 5));"
        out.println("Código fuente a inspeccionar:")
        out.println(source)
        val config =
            mapOf(
                "identifier_format" to "camelCase",
                "mandatory-variable-or-literal-in-println" to true,
            )
        val result = PrintScriptRunner.analyze(source.reader(), Version.V1_0, config)
        out.println("\nViolaciones de estilo detectadas por el Linter:")
        result.errors.forEach { out.println("  [LINTER] ${it.render()}") }
    }

    private fun demoFormatter(out: PrintWriter) {
        out.println("\n[4/5] Formateador de código real (Transformación AST y reglas de espaciado)")
        out.println("--------------------------------------------------------------------------------")
        val messySource = "let x:number=10;let y:number=20;\nprintln(\"Valor: \"+x);"
        out.println("Código original sin formato:")
        out.println(messySource)
        val config =
            mapOf(
                "enforce-spacing-around-equals" to true,
                "enforce-spacing-before-colon-in-declaration" to false,
                "enforce-spacing-after-colon-in-declaration" to true,
                "line-breaks-after-println" to 1,
            )
        val stringWriter = StringWriter()
        PrintScriptRunner.format(messySource.reader(), Version.V1_0, config, stringWriter)
        out.println("\nCódigo formateado resultante:")
        out.print(stringWriter.toString())
    }

    private fun demoErrorHandling(out: PrintWriter) {
        out.println("\n[5/5] Diagnóstico de errores de sintaxis real con Spans exactos")
        out.println("--------------------------------------------------------------------------------")
        val invalidSource = "let missingSemicolon: number = 42\nprintln(\"Error anterior\");"
        out.println("Código fuente inválido:")
        out.println(invalidSource)
        val result = PrintScriptRunner.validate(invalidSource.reader(), Version.V1_0)
        out.println("\nError sintáctico capturado y posicionado:")
        result.errors.forEach { out.println("  [VALIDATION] ${it.render()}") }
    }
}
