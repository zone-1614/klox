class Interpreter: Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = Environment()
    private var environment = globals

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity() = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>) =
                (System.currentTimeMillis() / 1000.0) as Double

            override fun toString() = "<native fn>"
        })
    }
    fun interpret(statements: List<Stmt?>) {
        try {
            for (stmt in statements)
                execute(stmt)
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitAssignExpr(expr: Expr.Companion.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
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

    override fun visitCallExpr(expr: Expr.Companion.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = arrayListOf<Any?>()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        val function = callee as LoxCallable
        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Companion.Grouping) = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Companion.Literal) = expr.value
    override fun visitLogicalExpr(expr: Expr.Companion.Logical): Any? {
        val left = evaluate(expr.left)
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

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

    override fun visitVariableExpr(expr: Expr.Companion.Variable): Any? {
        return environment.get(expr.name)
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

    private fun execute(stmt: Stmt?) = stmt?.accept(this)

    fun executeBlock(stmts: List<Stmt?>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (stmt in stmts) {
                execute(stmt)
            }
        } finally {
            this.environment = previous
        }
    }
    override fun visitBlockStmt(stmt: Stmt.Companion.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitExpressionStmt(stmt: Stmt.Companion.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Companion.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.Companion.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Companion.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Companion.Return) {
        var value: Any? = null
        if (stmt.value != null)
            value = evaluate(stmt.value)
        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Companion.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.Companion.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

}