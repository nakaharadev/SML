package com.ndev.sml

import sml.*
import sml.exception.UnresolvedClassNameException
import sml.spans.Span
import sml.spans.SpanTypeface
import sml.spans.SpannableText
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class SMLParser(private val sml: String, vararg classList: KClass<out Any>) {
    private val classList = classList.toList()

    private val tokenizer = Tokenizer(sml)

    var cacheOperator: CacheOperator? = null

    fun parse(prefix: String?): SMLObject {
        var tokens: List<Token>? = emptyList()

        if (cacheOperator != null) {
            tokens = cacheOperator?.transform(cacheOperator?.read())
        }

        if (sml.isEmpty())
            return SMLObject(emptyList())

        if (tokens?.isEmpty() == true) {
            tokens = tokenizer.tokenize()

            if (cacheOperator != null) {
                cacheOperator?.write(cacheOperator?.transform(tokens))
            }
        }

        val nodes = sliceToNodes(tokens!!)
        val list = ArrayList<Any>()
        for (node in nodes) {
            list.add(getObjectForNode(node, prefix) ?: continue)
        }

        return SMLObject(list)
    }

    private fun sliceToNodes(tokens: List<Token>): List<List<Token>> {
        return sliceFor(tokens, TokenType.START_DECLARE_NODE, TokenType.END_DECLARE_NODE)
    }

    private fun getObjectForNode(node: List<Token>, prefix: String?): Any? {
        val parsed = parseNode(node)
        val className = parsed["className"] as String
        val constructorParams = parsed["constructorParams"] as HashMap<String, Any?>
        val clazz = classList.find { it.simpleName == className }

        return if (prefix != null) {
            if (parsed["prefix"] != null && parsed["prefix"] == prefix) {
                getObject(constructorParams, clazz ?: throw UnresolvedClassNameException(
                    "Cant find class for name $className"
                )
                )
            } else null
        } else {
            getObject(constructorParams, clazz ?: throw UnresolvedClassNameException(
                "Cant find class for name $className"
            )
            )
        }
    }

    private fun parseNode(tokens: List<Token>): HashMap<String, Any?> {
        val result = hashMapOf<String, Any?>()
        result["className"] = findToken(tokens, TokenType.NODE_CLASS)?.lexeme

        val prefix = findToken(tokens, TokenType.NODE_PREFIX)?.lexeme ?: ""
        if (prefix.isNotEmpty())
            result["prefix"] = prefix

        val params = hashMapOf<String, Any?>()
        val slices = sliceParams(tokens)
        for (slice in slices) {
            val paramType = findToken(slice, TokenType.NODE_PARAM_TYPE)?.lexeme ?: ""
            if (paramType == "param") {
                val paramName = findToken(slice, TokenType.NODE_PARAM_NAME)?.lexeme ?: ""
                val operator = findToken(slice, TokenType.OPERATOR)
                if (operator?.lexeme.toString() == Operator.SET.strName) {
                    val type = findToken(slice, TokenType.DATA_TYPE)?.lexeme ?: ""
                    val identifier = findToken(slice, TokenType.IDENTIFIER)?.lexeme ?: ""
                    val obj = getObjectForType(type, identifier)

                    params[paramName] = obj
                }
            }
        }
        result["constructorParams"] = params

        return result
    }

    private fun getObjectForType(type: String, value: String): Any? {
        if (type.isEmpty())
            return null

        return when (type) {
            "string" -> value.trim('"')
            "int" -> value.toInt()
            "float" -> value.toFloat()
            else -> null
        }
    }

    private fun sliceParams(tokens: List<Token>): List<List<Token>> {
        return sliceFor(tokens, TokenType.NODE_START_PARAM, TokenType.NODE_END_PARAM)
    }

    private fun getObject(params: HashMap<String, Any?>, clazz: KClass<out Any>): Any? {
        val args = clazz.primaryConstructor!!.parameters.map {
            for (param in params) {
                if (param.key == it.name) {
                    if (isSpannableText(param.value as String?))
                        return@map getSpannableText(param.value as String)
                    return@map param.value
                }
            }
        }.toTypedArray()

        return clazz.primaryConstructor?.call(*args)
    }

    private fun isSpannableText(text: String?): Boolean {
        if (text == null)
            return false

        return """[A-Za-z]&[^&]*&""".toRegex().find(text) != null
    }

    private fun getSpannableText(text: String): SpannableText? {
        val spans = mutableListOf<Span>()
        val regex = """[A-Za-z]&[^&]*&""".toRegex()

        if (regex.find(text) == null)
            return null

        var correctText = text

        do {
            val elem = regex.find(correctText) ?: break

            val indicator = elem.value[0]
            val typeface = (SpanTypeface of indicator) ?: continue
            val span = Span(
                elem.range.first,
                elem.range.last - 2,
                typeface
            )

            correctText = correctText.replace(
                elem.value,
                elem.value.substring(2, elem.value.length - 1)
            )
            spans.add(span)
        } while (true)

        return SpannableText(correctText, spans)
    }

    private fun findToken(tokens: List<Token>, type: TokenType): Token? {
        return tokens.find { it.type == type }
    }

    private fun sliceFor(tokens: List<Token>, startType: TokenType, endType: TokenType): List<List<Token>> {
        val result = ArrayList<List<Token>>()
        var slice = ArrayList<Token>()

        var isNode = false
        for (token in tokens) {
            if (isNode) {
                if (token.type == endType) {
                    result.add(slice)
                    slice = ArrayList()
                    isNode = false
                } else {
                    slice.add(token)
                }
            }

            if (token.type == startType)
                isNode = true
        }

        return result
    }
}