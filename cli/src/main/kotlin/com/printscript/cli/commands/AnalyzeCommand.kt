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
import java.util.concurrent.Callable

@Command(
    name = "analyze",
    description = ["Ejecuta el análisis estático (linter) sobre un archivo fuente PrintScript."],
    mixinStandardHelpOptions = true,
)
class AnalyzeCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-c", "--config"], description = ["Ruta al archivo de configuración JSON del linter"])
    var configFile: File? = null

    @Option(names = ["-v", "--version"], defaultValue = "1.0", description = ["Versión del lenguaje (1.0 o 1.1)"])
    var versionStr: String = "1.0"

    override fun call(): Int {
        val out = spec?.commandLine()?.out ?: java.io.PrintWriter(System.out, true)
        val err = spec?.commandLine()?.err ?: java.io.PrintWriter(System.err, true)

        val targetFile = file
        if (targetFile == null || !targetFile.exists()) {
            err.println("Error: Archivo no encontrado ${targetFile?.path ?: ""}")
            return 2
        }

        val version = try {
            Version.from(versionStr)
        } catch (e: IllegalArgumentException) {
            err.println("Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
            return 2
        }

        val config = ConfigLoader.loadConfig(configFile)

        val result = targetFile.reader().use { reader ->
            PrintScriptRunner.analyze(reader, version, config)
        }

        if (result.errors.isNotEmpty()) {
            for (error in result.errors) {
                err.println(error.render())
            }
            return 1
        }

        out.println("Análisis completado sin violaciones de linter.")
        return 0
    }
}
