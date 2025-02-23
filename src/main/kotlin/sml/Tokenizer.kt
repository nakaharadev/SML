package sml

import sml.exception.SMLSyntaxException


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

            val titleTokens = parseNodeTitle(node.trim('\n'))
            if (titleTokens["prefix"] != null)
                tokens.add(titleTokens["prefix"]!!)
            tokens.add(titleTokens["className"]!!)

            tokens.addAll(parseNodeBody(node))

            tokens.add(Token(TokenType.END_DECLARE_NODE))
        }

        return tokens
    }

    private fun getNodesText(): List<String> {
        val split = """\n\}""".toRegex().split(smlData)
        return split
            .mapIndexed { index, s ->  if (index != split.size - 1) "$s\n}" else s }
            .filter { it.isNotEmpty() }.toList()
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

        val split = param.split('=', ignoreCase = false, limit = 2)
        if (split.size == 1)
            throw SMLSyntaxException("param should be initialized in $param")

        val paramType = split[0].split(':')[0]
        val paramName = split[0].split(':')[1].trim()

        result.add(Token(TokenType.NODE_PARAM_TYPE, paramType))
        result.add(Token(TokenType.NODE_PARAM_NAME, paramName))
        result.add(Token(TokenType.OPERATOR, Operator.SET.strName))
        val valueType = getValueType(split[1].trim())
        if (valueType != "exp") result.add(Token(TokenType.DATA_TYPE, valueType))
        if (valueType != "function") {
            if (valueType == "exp")
                result.addAll(parseExpression(split[1].trim()))
            else
                result.add(Token(TokenType.IDENTIFIER, split[1].trim()))
        } else result.addAll(parseFunctionCall(split[1].trim()))

        result.add(Token(TokenType.NODE_END_PARAM))

        return result
    }

    private fun parseExpression(value: String): List<Token> {
        val result = ArrayList<Token>()
        result.add(Token(TokenType.START_EXPRESSION))

        var identifier = ""
        var operator = ""
        var isOperator = false

        for (c in value) {
            if (c in "+-=<>!^*/%") {
                if (!isOperator) {
                    result.add(Token(TokenType.IDENTIFIER, identifier))
                    identifier = ""
                }
                isOperator = true
                operator += c
            } else {
                if (isOperator) {
                    result.add(Token(TokenType.OPERATOR, (Operator of operator).strName))
                    operator = ""
                }
                identifier += c
                isOperator = false
            }
        }

        result.add(Token(TokenType.END_EXPRESSION))

        return result
    }

    private fun parseFunctionCall(func: String): List<Token> {
        val result = ArrayList<Token>()
        result.add(Token(TokenType.FUNCTION_CALL_START))

        val funcParamsStr = Regex(pattern = """\(.*\)""", option = RegexOption.DOT_MATCHES_ALL).find(func)?.value ?: ""

        val params = getFunctionParams(funcParamsStr.trim('(', ')'))
        val funName = func.split('(')[0]
        result.add(Token(TokenType.IDENTIFIER, funName.trim()))

        for (param in params) {
            val type = checkType(param)
            result.add(Token(TokenType.FUNCTION_PARAM_START))
            result.add(Token(TokenType.DATA_TYPE, type))
            result.add(Token(TokenType.IDENTIFIER, param.trim()))
            result.add(Token(TokenType.FUNCTION_PARAM_END))
        }

        result.add(Token(TokenType.FUNCTION_CALL_END))

        return result
    }

    private fun checkType(value: String): String {
        val defType = getValueType(value)
        if (defType.isNotEmpty())
            return defType

        if ("""map ?\{.*\}""".toRegex().find(value) != null)
            return "map"
        if (value.startsWith("cls"))
            return "cls"
        if (value.contains("""[+\-=<>!^*/%]+""".toRegex()))
            return "exp"

        return ""
    }

    private fun getFunctionParams(func: String): List<String> {
        val result = ArrayList<String>()

        var param = ""
        var ignore = false
        var braces = 0

        for (c in func) {
            if (c in "{([") {
                braces += 1
                ignore = true
                param += c

                continue
            }
            if (c in "})]") {
                braces -= 1

                if (braces == 0)
                    ignore = false
                param += c

                continue
            }

            if (!ignore) {
                if (c == ',') {
                    result.add(param.trim())
                    param = ""
                } else {
                    param += c
                }
            } else param += c
        }

        result.add(param)

        return result
    }

    private fun getValueType(value: String): String {
        if (checkIsString(value))
            return "string"
        if (value.toIntOrNull() != null)
            return "int"
        if (value.toFloatOrNull() != null)
            return "float"

        if (value.matches("""[a-zA-Z]+\(.*\)""".toRegex()))
            return "function"

        return ""
    }

    private fun checkIsString(value: String): Boolean {
        return value.startsWith('"') && value.endsWith('"')
    }
}