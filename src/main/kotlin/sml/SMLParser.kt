package com.ndev.nml

import sml.spans.Span
import sml.spans.SpanTypeface
import sml.spans.SpannableText
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class SMLParser(private val nml: String, vararg classList: KClass<Any>) {
    private val classList = classList.toList()

    fun parse(prefix: String): SMLObject<Any> {
        if (nml.isEmpty())
            return SMLObject(emptyList())

        val nodesText = getNodesText(prefix)
        val list = ArrayList<Any>()
        for (node in nodesText) {
            list.add(getObjectForNode(node) ?: continue)
        }

        return SMLObject(list)
    }

    private fun getNodesText(prefix: String): List<String> {
        val regex = Regex(pattern = """$prefix : [A-Za-z]+ \{[^}]*\}""", options = setOf(RegexOption.IGNORE_CASE))
        return regex.findAll(nml).map { it.value }.toList()
    }

    private fun getObjectForNode(node: String): Any? {
        val parsed = parseNode(node)
        val className = parsed["className"] as String
        val constructorParams = parsed["constructorParams"] as HashMap<String, Any?>
        val clazz = classList.find { it.simpleName == className }

        return getObject(constructorParams, clazz ?: throw UnresolvedClassNameException(
            "Cant find class for name $className"
        ))
    }

    private fun parseNode(node: String): HashMap<String, Any?> {
        val result = hashMapOf<String, Any?>()
        result["className"] = getClassName(node)
        result["constructorParams"] = getConstructorParams(node)

        return result
    }

    private fun getClassName(node: String): String {
        return node.split("[:{]".toRegex())[1].trim()
    }

    private fun getConstructorParams(node: String): HashMap<String, Any?> {
        val result = hashMapOf<String, Any?>()

        val regex = """param:[a-zA-Z]+=.*""".toRegex()
        for (param in regex.findAll(node)) {
            val pair = parseParam(param.value)
            result[pair.first] = pair.second
        }

        return result
    }

    private fun parseParam(param: String): Pair<String, Any?> {
        val split = param.split('=')

        val name = param.split("[:=]".toRegex())[1]
        val obj = getObjectForParam(split.subList(1, split.size).joinToString('='.toString()))

        return Pair(name, obj)
    }

    private fun getObjectForParam(value: String): Any? {
        if (value == "null")
            return null

        return when (getTypeName(value)) {
            "string" -> value.trim('"')
            "int" -> value.toInt()
            "float" -> value.toFloat()
            "object" -> getParamObject(value)
            else -> null
        }
    }

    private fun getTypeName(value: String): String {
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

    private fun getParamObject(param: String): Any? {
        val newParam = param.replace("""".*"""".toRegex()) {
            return@replace "\"*${it.value.substring(1, it.value.length - 1)}\""
        }
        val params = newParam.split("[()]".toRegex())[1]
        val split = params.split('"')
            .filter { it.isNotEmpty() }

        val splitWithStrings = ArrayList<String>()
        for (elem in split) {
            if (!elem.startsWith('*'))
                splitWithStrings.addAll(elem.split(','))
            else splitWithStrings.add("\"${elem.substring(1)}\"")
        }

        var corrected = false
        val correctSplit = ArrayList<String>()
        for (i: Int in splitWithStrings.indices) {
            if (corrected) {
                corrected = false
                continue
            }

            if (splitWithStrings[i].endsWith('=')) {
                correctSplit.add(splitWithStrings[i].trim() + splitWithStrings[i + 1].trim())
                corrected = true
            } else correctSplit.add(splitWithStrings[i].trim())
        }

        val clazz = classList.find {
            it.simpleName == correctSplit[0]
        }

        correctSplit.removeAt(0)
        val args = correctSplit.map {
            val argSplit = it.split('=')
            Pair(argSplit[0], argSplit[1])
        }.toTypedArray()

        return getObject(
            hashMapOf(*args),
            clazz ?: throw UnresolvedClassNameException(
                "Cant find class for name ${correctSplit[0]}"
            )
        )
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
}