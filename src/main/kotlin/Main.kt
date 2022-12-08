import java.io.BufferedReader
import java.io.InputStreamReader
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
/*

var a = true;
if (a) { print "hehe"; }
var b = 1;
while (b < 3) { b = b + 1; for (var k = 0; k < 3; k = k + 1) { print k; print k + b; } print "-----"; }
 */
val interpreter = Interpreter()
var hadError = false
var hadRuntimeError = false

private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
//    run(String(bytes), Charset.defaultCharset())
    run(String(bytes))
    if (hadError)
        exitProcess(65)
    if (hadRuntimeError)
        exitProcess(70)
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
    val parser = Parser(tokens)
    val statements = parser.parse()

    if (hadError) return
    interpreter.interpret(statements)
//    println(AstPrinter().print(expression))
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    println("[line $line] Error $where: $message")
    hadError = true
}

fun error1(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, "at end", message)
    } else {
        report(token.line, "at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: RuntimeError) {
    println("${error.message}\n[line ${error.token.line}]")
    hadRuntimeError = true
}