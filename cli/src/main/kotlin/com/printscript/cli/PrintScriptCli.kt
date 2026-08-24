package com.printscript.cli

import com.printscript.cli.commands.AnalyzeCommand
import com.printscript.cli.commands.DemoCommand
import com.printscript.cli.commands.ExecuteCommand
import com.printscript.cli.commands.FormatCommand
import com.printscript.cli.commands.ValidateCommand
import com.printscript.cli.util.CommandLineTokenizer
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "printscript",
    description = ["CLI oficial de PrintScript para validar, ejecutar, formatear y analizar código."],
    mixinStandardHelpOptions = true,
    version = ["PrintScript 1.0.0"],
    subcommands = [
        ValidateCommand::class,
        ExecuteCommand::class,
        FormatCommand::class,
        AnalyzeCommand::class,
        DemoCommand::class,
    ],
)
class PrintScriptCli : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    var customReader: BufferedReader? = null

    override fun call(): Int {
        val out = spec?.commandLine()?.out ?: PrintWriter(System.out, true)
        val err = spec?.commandLine()?.err ?: PrintWriter(System.err, true)
        val reader = customReader ?: BufferedReader(InputStreamReader(System.`in`))

        printBanner(out)
        return runInteractiveLoop(reader, out, err)
    }

    private fun runInteractiveLoop(
        reader: BufferedReader,
        out: PrintWriter,
        err: PrintWriter,
    ): Int {
        generateSequence {
            out.print("printscript> ")
            out.flush()
            reader.readLine()
        }.takeWhile { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) true else processLine(trimmed, out, err)
        }.count()
        return 0
    }

    private fun processLine(
        line: String,
        out: PrintWriter,
        err: PrintWriter,
    ): Boolean {
        if (line.equals("exit", ignoreCase = true) ||
            line.equals("quit", ignoreCase = true) ||
            line.equals("salir", ignoreCase = true)
        ) {
            out.println("Sesión finalizada.")
            return false
        }
        if (line.equals("help", ignoreCase = true) || line.equals("ayuda", ignoreCase = true)) {
            printHelp(out)
            return true
        }

        val args = CommandLineTokenizer.tokenize(line)
        val cmd = CommandLine(PrintScriptCli()).setOut(out).setErr(err)
        executeArguments(cmd, args)
        return true
    }

    @Suppress("SpreadOperator")
    private fun executeArguments(
        cmd: CommandLine,
        args: List<String>,
    ): Int = cmd.execute(*args.toTypedArray())

    private fun printBanner(out: PrintWriter) {
        out.println("================================================================================")
        out.println("                   PRINTSCRIPT - CONSOLA INTERACTIVA (CLI)                      ")
        out.println("================================================================================")
        out.println("Escriba 'help' para ver la lista de comandos disponibles o 'exit' para salir.\n")
    }

    private fun printHelp(out: PrintWriter) {
        out.println("Comandos disponibles:")
        out.println("  execute <archivo.ps> | --code \"...\" [-v 1.0|1.1]   Ejecuta un script o código")
        out.println("  format <archivo.ps> | --code \"...\" [-c config.json] [--preview]   Formatea código")
        out.println("  analyze <archivo.ps> | --code \"...\" [-c config.json] [-v 1.0|1.1]  Analiza con Linter")
        out.println("  validate <archivo.ps> | --code \"...\" [-v 1.0|1.1]                  Valida sintaxis y tipos")
        out.println("  demo / e2e                                                         Ejecuta demo E2E guiada")
        out.println("  help / ayuda                                                       Muestra esta ayuda")
        out.println("  exit / quit / salir                                                Cierra la consola")
    }
}
