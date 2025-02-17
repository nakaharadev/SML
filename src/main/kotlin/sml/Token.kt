package sml

data class Token(
    val type: TokenType,
    val lexeme: String = ""
) {
    override fun toString(): String {
        return if (lexeme.isEmpty())
            "Token(type=$type)"
        else "Token(type=$type, lexeme=$lexeme)"
    }

    fun toStringSimplify(): String {
        return "$type;$lexeme"
    }
}