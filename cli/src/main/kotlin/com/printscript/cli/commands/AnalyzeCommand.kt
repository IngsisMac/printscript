package com.printscript.cli.commands

import com.printscript.cli.util.ConfigLoader
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
import java.util.concurrent.Callable

@Command(
    name = "analyze",
    aliases = ["analyzing", "lint"],
    description = ["Ejecuta el análisis estático (linter) sobre un archivo o código PrintScript."],
    mixinStandardHelpOptions = true,
)
class AnalyzeCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", arity = "0..1", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-s", "--src", "--code"], description = ["Código fuente directo para analizar"])
    var inlineCode: String? = null

    @Option(names = ["-c", "--config"], description = ["Ruta al archivo de configuración JSON del linter"])
    var configFile: File? = null

    @Option(names = ["-v", "--version"], defaultValue = "1.0", description = ["Versión del lenguaje (1.0 o 1.1)"])
    var versionStr: String = "1.0"

    @Option(names = ["--progress"], description = ["Muestra el progreso durante el parsing en pantalla"])
    var showProgress: Boolean = false

    override fun call(): Int {
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val target = resolveReaderAndVersion(err) ?: return 2
        return analyzeScript(target.first, target.second)
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

    private fun analyzeScript(
        source: Reader,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val config = ConfigLoader.loadConfig(configFile)

        val result =
            source.use { reader ->
                PrintScriptRunner.analyze(reader, version, config, onProgress = createProgressCallback(out))
            }

        if (result.errors.isNotEmpty()) {
            val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
            result.errors.forEach { err.println(it.render()) }
            return 1
        }

        out.println("Análisis completado sin violaciones de linter.")
        return 0
    }

    private fun createProgressCallback(out: PrintWriter): (Int, com.printscript.ast.Statement) -> Unit =
        { count, stmt ->
            if (showProgress) {
                out.println("[Progreso] Sentencia #$count parseada (Línea ${stmt.span.start.line})")
            }
        }
}
