package sml.script.functions

import sml.script.types.Obj
import kotlin.reflect.full.primaryConstructor

abstract class Function {
    abstract fun call(vararg args: Obj?): Any?

    companion object {
        private val funMap = hashMapOf(
            "obj" to ObjFunction::class
        )

        fun find(name: String): Function? {
            return funMap[name]?.primaryConstructor?.call()
        }
    }
}