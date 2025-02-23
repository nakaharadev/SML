package sml

enum class Operator(val symbol: String) {
    SET("="),
    EQUALS("=="),
    NOT_EQUALS("!="),
    MORE_THAT(">"),
    LESS_THAT("<"),
    MORE_OR_EQUALS(">="),
    LESS_OR_EQUALS("<="),
    ADD("+"),
    SUB("-"),
    DIV("/"),
    MUL("*"),
    ICR("++"),
    DEC("--"),
    ADD_AND_SET("+="),
    SUB_AND_SET("-="),
    UNDEFINED("");

    val strName: String
        get() { return name.lowercase() }

    companion object {
        infix fun of(value: String): Operator {
            return entries.find { it.symbol == value } ?: UNDEFINED
        }
    }
}