package sml.script.types

class Str(val value: String) : Obj() {
    override fun toString(): String {
        return "Str(value=\"$value\")"
    }
}