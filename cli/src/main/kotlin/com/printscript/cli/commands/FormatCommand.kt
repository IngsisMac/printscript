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
import java.util.concurrent.Callable

@Command(
    name = "format",
    aliases = ["formatting"],
    description = ["Formatea un archivo fuente PrintScript de acuerdo a reglas de estilo."],
    mixinStandardHelpOptions = true,
)
class FormatCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-c", "--config"], description = ["Ruta al archivo de configuración JSON"])
    var configFile: File? = null

    @Option(names = ["-v", "--version"], defaultValue = "1.0", description = ["Versión del lenguaje (1.0 o 1.1)"])
    var versionStr: String = "1.0"

    @Option(
        names = ["-o", "--output"],
        description = ["Ruta de salida para guardar el código formateado en un nuevo archivo"],
    )
    var outputFile: File? = null

    @Option(
        names = ["-p", "--preview"],
        description = ["Muestra el código formateado en consola sin modificar archivos en disco"],
    )
    var preview: Boolean = false

    @Option(names = ["--progress"], description = ["Muestra el progreso durante el parsing en pantalla"])
    var showProgress: Boolean = false

    override fun call(): Int {
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val target = resolveTargetAndVersion(err) ?: return 2
        return formatScript(target.first, target.second)
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

    private fun formatScript(
        targetFile: File,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val config = ConfigLoader.loadConfig(configFile)

        return when {
            preview -> formatPreview(targetFile, version, config, out)
            outputFile != null -> formatToFile(targetFile, outputFile!!, version, config, err)
            else -> formatToFile(targetFile, targetFile, version, config, err)
        }
    }

    private fun formatPreview(
        targetFile: File,
        version: Version,
        config: Map<String, Any?>,
        out: PrintWriter,
    ): Int {
        val result =
            targetFile.reader().use { reader ->
                PrintScriptRunner.format(reader, version, config, out, createProgressCallback())
            }

        if (result.errors.isNotEmpty()) {
            val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
            result.errors.forEach { err.println(it.render()) }
            return 1
        }
        out.flush()
        return 0
    }

    private fun formatToFile(
        source: File,
        target: File,
        version: Version,
        config: Map<String, Any?>,
        err: PrintWriter,
    ): Int {
        val temp = createTempForTarget(source, target)
        val result = writeFormattedToTemp(source, temp, version, config)
        if (result.errors.isNotEmpty()) {
            temp.delete()
            result.errors.forEach { err.println(it.render()) }
            return 1
        }
        replaceTargetFile(temp, target)
        return 0
    }

    private fun createTempForTarget(
        source: File,
        target: File,
    ): File {
        target.parentFile?.mkdirs()
        return File.createTempFile("ps_fmt", ".tmp", target.parentFile ?: source.parentFile)
    }

    private fun writeFormattedToTemp(
        source: File,
        temp: File,
        version: Version,
        config: Map<String, Any?>,
    ) = temp.bufferedWriter().use { writer ->
        source.reader().use { reader ->
            PrintScriptRunner.format(reader, version, config, writer, createProgressCallback())
        }
    }

    private fun replaceTargetFile(
        temp: File,
        target: File,
    ) {
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    private fun createProgressCallback(): (Int, com.printscript.ast.Statement) -> Unit {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        return { count, stmt ->
            if (showProgress) {
                out.println("[Progreso] Sentencia #$count parseada (Línea ${stmt.span.start.line})")
            }
        }
    }
}
