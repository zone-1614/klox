class Interpreter: Expr.Visitor<Any?> {
    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Companion.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            TokenType.PLUS -> if ( (left is Double).and(right is Double) ) {
                (left as Double).plus(right as Double)
            } else if ( (left is String).and(right is String) ) {
                (left as String).plus(right as String)
            } else {
                null
            }
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            else -> null
        }
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping) = evaluate(expr)

    override fun visitLiteralExpr(expr: Expr.Companion.Literal) = expr.value

    override fun visitUnaryExpr(expr: Expr.Companion.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            else -> null
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number. ")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if ((left is Double).and(right is Double)) return
        throw RuntimeError(operator, "Operands must be numbers. ")
    }

    private fun isTruthy(any: Any?): Boolean {
        if (any == null) return false
        if (any is Boolean) return any
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        return a == b
    }

    private fun stringify(any: Any?): String {
        if (any == null) return "nil"
        if (any is Double) {
            var text = any.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return any.toString()
    }

    private fun evaluate(expr: Expr) = expr.accept(this)
}