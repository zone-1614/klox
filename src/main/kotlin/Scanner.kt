class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = arrayListOf()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    companion object {
        private val keywords: Map<String, TokenType> = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun scanToken() {
        when (val c: Char = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            '!' -> addToken(if (match('=')) { TokenType.BANG_EQUAL } else { TokenType.BANG })
            '=' -> addToken(if (match('=')) { TokenType.EQUAL_EQUAL } else { TokenType.EQUAL })
            '<' -> addToken(if (match('=')) { TokenType.LESS_EQUAL } else { TokenType.LESS })
            '>' -> addToken(if (match('=')) { TokenType.GREATER_EQUAL } else { TokenType.GREATER })

            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance() // comment
                } else if (match('*')) { // block comment
                    while ( !( peek() == '*' && peekNext() == '/' ) ) {
                        advance()
                        if (isAtEnd()) {
                            error(line, "Block comment can not match */ ")
                            return
                        }
                    }
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            // ignore whitespace
            ' ' -> { }
            '\r' -> { }
            '\t' -> { }

            '\n' -> line++

            '"' -> string()
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    error(line, "Unexpected character. ")
                }
            }
        }
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun advance(): Char = source[current++]

    private fun addToken(type: TokenType) = addToken(type, null)

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun peek(): Char = if (isAtEnd()) { '\n' } else { source[current] }
    private fun peekNext(): Char = if (current + 1 >= source.length) { '\n' } else { source[current + 1] }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++
            advance()
        }
        if (isAtEnd()) {
            error(line, "Unterminated string. ")
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'
    private fun isAlpha(c: Char): Boolean = (c in 'a'..'z').or(c in 'A'..'Z').or(c == '_')
    private fun isAlphaNumeric(c: Char): Boolean = isDigit(c).or(isAlpha(c))

    private fun number() {
        while (isDigit(peek()))
            advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance() // consume '.'
            while (isDigit(peek()))
                advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek()))
            advance()
        val text = source.substring(start, current)
        var type = keywords[text]
        if (type == null)
            type = TokenType.IDENTIFIER
        addToken(type)
    }
}