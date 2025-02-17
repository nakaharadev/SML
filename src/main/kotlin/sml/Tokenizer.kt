package sml


class Tokenizer(val smlData: String) {
    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        tokens.addAll(parseNodes())

        return tokens
    }

    private fun parseNodes(): List<Token> {
        val tokens = mutableListOf<Token>()

        val nodes = getNodesText()
        for (node in nodes) {
            tokens.add(Token(TokenType.START_DECLARE_NODE))

            val titleTokens = parseNodeTitle(node)
            if (titleTokens["prefix"] != null)
                tokens.add(titleTokens["prefix"]!!)
            tokens.add(titleTokens["className"]!!)


            tokens.addAll(parseNodeBody(node))

            tokens.add(Token(TokenType.END_DECLARE_NODE))
        }

        return tokens
    }

    private fun getNodesText(): List<String> {
        return Regex(pattern = """[a-zA-Z ]*:?[A-Za-z ]*\{[^}]*\}""", options = setOf(RegexOption.IGNORE_CASE))
            .findAll(smlData).map { it.value }.toList()
    }

    private fun parseNodeTitle(node: String): HashMap<String, Token?> {
        val result = hashMapOf<String, Token?>()

        var title = """[a-zA-Z ]*:?[A-Za-z ]*\{""".toRegex().find(node)?.value ?: return hashMapOf()
        title = title.trim('{')
        val split = title.split(':')
        if (split.size == 1) {
            result["className"] = Token(TokenType.NODE_CLASS, split[0].trim())
        } else {
            result["prefix"] = Token(TokenType.NODE_PREFIX, split[0].trim())
            result["className"] = Token(TokenType.NODE_CLASS, split[1].trim())
        }

        return result
    }

    private fun parseNodeBody(node: String): List<Token> {
        val result = mutableListOf<Token>()

        val params = """param:[a-zA-Z ]+= ?.*""".toRegex().findAll(node).map { it.value }.toList()
        if (params.isNotEmpty())
            result.add(Token(TokenType.NODE_START_BODY))

        for (param in params) {
            result.addAll(parseParam(param))
        }

        if (params.isNotEmpty())
            result.add(Token(TokenType.NODE_END_BODY))

        return result
    }

    private fun parseParam(param: String): List<Token> {
        val result = mutableListOf<Token>()

        result.add(Token(TokenType.NODE_START_PARAM))

        val split = param.split('=')
        if (split.size == 1)
            throw SMLSyntaxException("param should be initialized in $param")

        val paramType = split[0].split(':')[0]
        val paramName = split[0].split(':')[1].trim()

        result.add(Token(TokenType.NODE_PARAM_TYPE, paramType))
        result.add(Token(TokenType.NODE_PARAM_NAME, paramName))
        result.add(Token(TokenType.OPERATOR, Operator.SET.strName))
        result.add(Token(TokenType.DATA_TYPE, getValueType(split[1].trim())))
        result.add(Token(TokenType.IDENTIFIER, split[1].trim()))

        result.add(Token(TokenType.NODE_END_PARAM))

        return result
    }

    private fun getValueType(value: String): String {
        if (checkIsString(value))
            return "string"
        if (value.toIntOrNull() != null)
            return "int"
        if (value.toFloatOrNull() != null)
            return "float"

        if (value.split('(')[0] == "obj")
            return "object"

        return ""
    }

    private fun checkIsString(value: String): Boolean {
        return value.startsWith('"') && value.endsWith('"')
    }
}