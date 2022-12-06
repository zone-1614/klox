import java.lang.RuntimeException

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (e: ParseError) {
            null
        }
    }

    /**
     * expression -> equality
     */
    private fun expression(): Expr = equality()

    /**
     * equality -> comparison ( ( "!=" | "==" ) comparison )*
     */
    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Companion.Binary(expr, operator, right)
        }
        return expr
    }

    /**
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
     */
    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Companion.Binary(expr, operator, right)
        }
        return expr
    }

    /**
     * term -> factor ( ( "-" | "+" ) factor )*
     */
    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Companion.Binary(expr, operator, right)
        }
        return expr
    }

    /**
     * factor -> unary ( ( "*" | "/" ) unary )*
     */
    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Companion.Binary(expr, operator, right)
        }
        return expr
    }

    /**
     * unary -> ( "!" | "-" ) unary
     *        | primary
     */
    private fun unary(): Expr {
        while (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Companion.Unary(operator, right)
        }
        return primary()
    }

    /**
     * primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
     */
    private fun primary(): Expr {
        if (match(TokenType.FALSE))
            return Expr.Companion.Literal(false)
        if (match(TokenType.TRUE))
            return Expr.Companion.Literal(true)
        if (match(TokenType.NIL))
            return Expr.Companion.Literal(null)
        if (match(TokenType.NUMBER, TokenType.STRING))
            return Expr.Companion.Literal(previous().literal)
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Except ')' after expression. ")
            return Expr.Companion.Grouping(expr)
        }
        throw error(peek(), "Expect expression. ")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String) {
        if (check(type)) advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd())
            return false
        return type == peek().type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == TokenType.EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current-1]

    private fun error(type: Token, message: String): ParseError {
        error(type, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)  return
            when (peek().type) {
                TokenType.CLASS -> { }
                TokenType.FUN -> { }
                TokenType.VAR -> { }
                TokenType.FOR -> { }
                TokenType.IF -> { }
                TokenType.WHILE -> { }
                TokenType.PRINT -> { }
                TokenType.RETURN -> { }
                else -> return
            }
            advance()
        }
    }

    companion object {
        class ParseError: RuntimeException()
    }
}