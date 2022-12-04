package tool

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output_directory>")
        exitProcess(64)
    }
    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf(
        "Binary - left: Expr, operator: Token, right: Expr",
        "Grouping - expression: Expr",
        "Literal - value: Any?",
        "Unary - operator: Token, right: Expr"
    ))
}

private fun defineAst(
    outputDir: String,
    baseName: String,
    types: List<String>
) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.apply {
        println("abstract class $baseName {")
        defineVisitor(writer, baseName, types)
        println("\tcompanion object {")
        for (item in types) {
            val className = item.split("-")[0].trim()
            val fields = item.split("-")[1].trim()
            defineType(writer, baseName, className, fields)
        }
        println("\t}")
        println("\tabstract fun <R> accept(visitor: Visitor<R>): R")
        println("}")
        close()
    }
}

private fun defineType(
    writer: PrintWriter,
    baseName: String,
    className: String,
    fields: String
) {
    val fieldList = fields.split(",")
    writer.apply {
        println("\t\tclass $className(")
        for (field in fieldList) {
            println("\t\t\tval ${field.trim()},")
        }
        println("\t\t): $baseName() {")
        println("\t\t\toverride fun <R> accept(visitor: Visitor<R>): R {")
        println("\t\t\t\treturn visitor.visit$className$baseName(this)")
        println("\t\t\t}")
        println("\t\t}")
        println()
    }
}

private fun defineVisitor(
    writer: PrintWriter,
    baseName: String,
    types: List<String>
) {
    writer.apply {
        println("\tinterface Visitor<R> {")
        for (type in types) {
            val typename = type.split("-")[0].trim()
            println("\t\tfun visit$typename$baseName(${baseName.toLowerCase()}: $typename): R")
        }
        println("\t}")
        println()
    }
}
