import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

var hadError = false

private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
//    run(String(bytes), Charset.defaultCharset())
    if (hadError)
        exitProcess(65)
}

private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null)
            break;
        run(line)
        hadError = false
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

}

fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    println("[line $line] Error $where: $message")
    hadError = true
}