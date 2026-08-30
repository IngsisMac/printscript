package com.printscript.cli.commands

import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import com.printscript.runner.PrintScriptRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec
import java.io.File
import java.io.PrintWriter
import java.io.Reader
import java.io.StringReader
import java.util.Scanner
import java.util.concurrent.Callable

@Command(
    name = "execute",
    aliases = ["execution", "run"],
    description = ["Ejecuta un archivo fuente o código directo PrintScript."],
    mixinStandardHelpOptions = true,
)
class ExecuteCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", arity = "0..1", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-s", "--src", "--code"], description = ["Código fuente directo para ejecutar"])
    var inlineCode: String? = null

    @Option(names = ["-v", "--version"], defaultValue = "1.0", description = ["Versión del lenguaje (1.0 o 1.1)"])
    var versionStr: String = "1.0"

    @Option(names = ["--progress"], description = ["Muestra el progreso durante el parsing en pantalla"])
    var showProgress: Boolean = false

    override fun call(): Int {
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val target = resolveReaderAndVersion(err) ?: return 2
        return runScript(target.first, target.second)
    }

    private fun resolveReaderAndVersion(err: PrintWriter): Pair<Reader, Version>? {
        val reader = resolveSourceReader(err) ?: return null
        val version =
            Version.from(versionStr).getOrElse {
                err.println("Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
                return null
            }
        return reader to version
    }

    private fun resolveSourceReader(err: PrintWriter): Reader? {
        val code = inlineCode
        val targetFile = file
        return when {
            code != null -> StringReader(code)
            targetFile != null -> {
                if (!targetFile.exists()) {
                    err.println("Error: Archivo no encontrado ${targetFile.path}")
                    null
                } else {
                    targetFile.reader()
                }
            }
            else -> {
                err.println("Error: Se debe proporcionar un archivo fuente o la opción --code.")
                null
            }
        }
    }

    private fun runScript(
        source: Reader,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val emitter = OutputEmitter { line -> out.println(line) }
        val input = createInputSource(out)
        val result =
            source.use { reader ->
                PrintScriptRunner.execute(
                    reader,
                    version,
                    emitter,
                    input,
                    env = EnvSource.SYSTEM,
                    onProgress = createProgressCallback(out),
                )
            }

        return handleErrors(result.errors)
    }

    private fun handleErrors(errors: List<com.printscript.common.PrintScriptError>): Int {
        if (errors.isEmpty()) return 0
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        errors.forEach { err.println(it.render()) }
        return 1
    }

    private fun createProgressCallback(out: PrintWriter): (Int, com.printscript.ast.Statement) -> Unit =
        { count, stmt ->
            if (showProgress) {
                out.println("[Progreso] Sentencia #$count parseada (Línea ${stmt.span.start.line})")
            }
        }

    private fun createInputSource(out: PrintWriter): InputSource {
        val scanner = Scanner(System.`in`)
        return InputSource { prompt ->
            if (prompt.isNotEmpty()) {
                out.print(prompt)
                out.flush()
            }
            if (scanner.hasNextLine()) scanner.nextLine() else ""
        }
    }
}
