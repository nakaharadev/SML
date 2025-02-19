package sml.script.functions

import sml.script.types.Cls
import sml.script.types.Map
import sml.script.types.Obj

class ObjFunction : Function() {
    override fun call(vararg args: Obj?): Any? {
        return (args[0] as Cls).create(*getParams(*args))
    }

    private fun getParams(vararg args: Obj?): Array<Any?> {
        val cls = args[0] as Cls
        val clsParams = cls.getConstructorParamsList()
        val map = args[1] as Map

        val params = clsParams.map {
            return@map map[it.name!!]
        }

        return params.toTypedArray()
    }
}