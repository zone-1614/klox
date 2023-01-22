class LoxFunction(
    private val declaration: Stmt.Companion.Function,
    private val closure: Environment
) : LoxCallable {
    override fun arity() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
        for (i in 0 until declaration.params.size)
            environment.define(declaration.params[i].lexeme, arguments[i])

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}