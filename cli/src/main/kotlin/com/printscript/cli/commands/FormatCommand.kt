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
    name = "format",
    aliases = ["formatting"],
    description = ["Formatea un archivo fuente o código directo PrintScript de acuerdo a reglas de estilo."],
    mixinStandardHelpOptions = true,
)
class FormatCommand : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    @Parameters(index = "0", arity = "0..1", description = ["Ruta al archivo fuente .ps"])
    var file: File? = null

    @Option(names = ["-s", "--src", "--code"], description = ["Código fuente directo para formatear"])
    var inlineCode: String? = null

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
        val version =
            Version.from(versionStr).getOrElse {
                err.println("Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
                return 2
            }
        return formatScript(version, err)
    }

    private fun formatScript(
        version: Version,
        err: PrintWriter,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val config = ConfigLoader.loadConfig(configFile)
        val code = inlineCode
        val targetFile = file

        return when {
            code != null -> formatInline(code, version, config, out, err)
            targetFile != null -> formatFromFile(targetFile, version, config, out, err)
            else -> {
                err.println("Error: Se debe proporcionar un archivo fuente o la opción --code.")
                2
            }
        }
    }

    private fun formatInline(
        code: String,
        version: Version,
        config: Map<String, Any?>,
        out: PrintWriter,
        err: PrintWriter,
    ): Int =
        if (outputFile != null) {
            val writer = outputFile!!.bufferedWriter()
            val result =
                StringReader(code).use { reader ->
                    PrintScriptRunner.format(reader, version, config, writer, createProgressCallback())
                }
            writer.flush()
            handleFormatErrors(result.errors, err)
        } else {
            formatPreview(StringReader(code), version, config, out, err)
        }

    private fun formatFromFile(
        targetFile: File,
        version: Version,
        config: Map<String, Any?>,
        out: PrintWriter,
        err: PrintWriter,
    ): Int {
        if (!targetFile.exists()) {
            err.println("Error: Archivo no encontrado ${targetFile.path}")
            return 2
        }
        return when {
            preview -> formatPreview(targetFile.reader(), version, config, out, err)
            outputFile != null -> formatToFile(targetFile, outputFile!!, version, config, err)
            else -> formatToFile(targetFile, targetFile, version, config, err)
        }
    }

    private fun formatPreview(
        source: Reader,
        version: Version,
        config: Map<String, Any?>,
        out: PrintWriter,
        err: PrintWriter,
    ): Int {
        val result =
            source.use { reader ->
                PrintScriptRunner.format(reader, version, config, out, createProgressCallback())
            }
        out.println()
        return handleFormatErrors(result.errors, err).also { out.flush() }
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
            return handleFormatErrors(result.errors, err)
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

    private fun handleFormatErrors(
        errors: List<com.printscript.common.PrintScriptError>,
        err: PrintWriter,
    ): Int {
        if (errors.isEmpty()) return 0
        errors.forEach { err.println(it.render()) }
        return 1
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
