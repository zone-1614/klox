import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.text.StringBuilder

class AstPrinter: Expr.Visitor<String> {
    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinaryExpr(expr: Expr.Companion.Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping) = parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Expr.Companion.Literal) = if (expr.value == null) "nil" else expr.value.toString()

    override fun visitUnaryExpr(expr: Expr.Companion.Unary) = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr) = StringBuilder().apply {
        append("($name")
        exprs.forEach {
            append(" ${it.accept(this@AstPrinter)}")
        }
        append(")")
    }.toString()

}

fun main() {
//    val expr = Expr.Companion.Binary(
//        Expr.Companion.Unary(Token(TokenType.MINUS, "-", null, 1), Expr.Companion.Literal(123)),
//        Token(TokenType.STAR, "*", null, 1),
//        Expr.Companion.Grouping(Expr.Companion.Literal(23.23))
//    )
//    println(AstPrinter().print(expr))
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null) {
            println("no input, exit")
            break
        }
        val scanner = Scanner(line)
        val tokens = scanner.scanTokens()
        println(tokens)
        val parser = Parser(tokens)
        val expr = parser.parse()
        println(AstPrinter().print(expr!!))
    }
}