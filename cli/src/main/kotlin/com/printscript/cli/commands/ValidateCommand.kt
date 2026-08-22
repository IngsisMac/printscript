package com.printscript.cli.commands

import com.printscript.common.Version
import com.printscript.runner.PrintScriptRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec
import java.io.File
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "validate",
    aliases = ["validation", "check"],
    description = ["Valida la sintaxis y semántica de un archivo PrintScript sin ejecutarlo."],
    mixinStandardHelpOptions = true,
)
class ValidateCommand : Callable<Int> {
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
        val targetFile = file ?: return printError(err, "Error: Archivo no encontrado ")
        if (!targetFile.exists()) return printError(err, "Error: Archivo no encontrado ${targetFile.path}")

        val version =
            Version.from(versionStr).getOrElse {
                return printError(err, "Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
            }

        return validateScript(targetFile, version)
    }

    private fun validateScript(
        targetFile: File,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)

        val progressCallback: (Int, com.printscript.ast.Statement) -> Unit = { count, stmt ->
            if (showProgress) {
                out.println("[Progreso] Sentencia #$count parseada (Línea ${stmt.span.start.line})")
            }
        }

        val result =
            targetFile.reader().use { reader ->
                PrintScriptRunner.validate(reader, version, progressCallback)
            }

        if (result.errors.isNotEmpty()) {
            result.errors.forEach { err.println(it.render()) }
            return 1
        }

        out.println("Validación completada sin errores.")
        return 0
    }

    private fun printError(
        err: PrintWriter,
        message: String,
    ): Int {
        err.println(message)
        return 2
    }
}
