package com.printscript.cli.commands

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
import java.util.Scanner
import java.util.concurrent.Callable

@Command(
    name = "execute",
    aliases = ["execution", "run"],
    description = ["Ejecuta un archivo fuente PrintScript."],
    mixinStandardHelpOptions = true,
)
class ExecuteCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-v", "--version"], defaultValue = "1.0", description = ["Versión del lenguaje (1.0 o 1.1)"])
    var versionStr: String = "1.0"

    @Option(names = ["--progress"], description = ["Muestra el progreso durante el parsing en pantalla"])
    var showProgress: Boolean = false

    override fun call(): Int {
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val target = resolveTargetAndVersion(err) ?: return 2
        return runScript(target.first, target.second)
    }

    private fun resolveTargetAndVersion(err: PrintWriter): Pair<File, Version>? {
        val targetFile = file
        if (targetFile == null || !targetFile.exists()) {
            val path = targetFile?.path?.let { " $it" } ?: " "
            err.println("Error: Archivo no encontrado$path")
            return null
        }
        val version =
            Version.from(versionStr).getOrElse {
                err.println("Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
                return null
            }
        return targetFile to version
    }

    private fun runScript(
        targetFile: File,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val emitter = OutputEmitter { line -> out.println(line) }
        val input = createInputSource(out)
        val result =
            targetFile.reader().use { reader ->
                PrintScriptRunner.execute(reader, version, emitter, input, onProgress = createProgressCallback(out))
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
