package sml.script.types

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Cls(private val clazz: KClass<out Any>) : Obj() {
    fun create(vararg args: Any?): Any? {
        return clazz.primaryConstructor?.call(*args)
    }

    fun getConstructorParamsList(): List<KParameter> {
        return clazz.primaryConstructor!!.parameters
    }

    override fun toString(): String {
        return "Cls(clazz=${clazz.simpleName})"
    }
}