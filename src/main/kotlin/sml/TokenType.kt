package sml

enum class TokenType {
    START_DECLARE_NODE,
    END_DECLARE_NODE,
    NODE_PREFIX,
    NODE_CLASS,
    NODE_START_BODY,
    NODE_END_BODY,
    NODE_START_PARAM,
    NODE_END_PARAM,
    NODE_PARAM_TYPE,
    NODE_PARAM_NAME,
    OPERATOR,
    DATA_TYPE,
    IDENTIFIER,
    FUNCTION_CALL_START,
    FUNCTION_CALL_END,
    FUNCTION_PARAM_START,
    FUNCTION_PARAM_END,
    FUNCTION_NAME,
    START_EXPRESSION,
    END_EXPRESSION;

    companion object {
        infix fun of(name: String): TokenType? {
            return entries.find { it.name == name.uppercase() }
        }
    }
}