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

    override fun call(): Int {
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val targetFile = file ?: return printError(err, "Error: Archivo no encontrado ")
        if (!targetFile.exists()) return printError(err, "Error: Archivo no encontrado ${targetFile.path}")

        val version =
            Version.from(versionStr).getOrElse {
                return printError(err, "Error: Versión no válida '$versionStr'. Usar 1.0 o 1.1.")
            }

        return runScript(targetFile, version)
    }

    private fun runScript(
        targetFile: File,
        version: Version,
    ): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val outputEmitter = OutputEmitter { line -> out.println(line) }
        val inputSource = createInputSource(out)

        val result =
            targetFile.reader().use { reader ->
                PrintScriptRunner.execute(reader, version, outputEmitter, inputSource)
            }

        if (result.errors.isNotEmpty()) {
            result.errors.forEach { err.println(it.render()) }
            return 1
        }
        return 0
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

    private fun printError(
        err: PrintWriter,
        message: String,
    ): Int {
        err.println(message)
        return 2
    }
}
