package sml.script.types

class Map(value: String) : Obj() {
    private val hashMap = HashMap<String, Any?>()

    operator fun get(key: String): Any? {
        return hashMap[key]
    }

    operator fun set(key: String, value: Any?) {
        hashMap[key] = value
    }

    init {
        val regex = """[a-zA-Z0-9 ]+:[^}]*""".toRegex()
        val lines = regex.findAll(value).map { it.value }.toList()
        for (line in lines) {
            val split = line.split(':')
            hashMap[split[0].trim()] = split[1].trim().trim('"')
        }
    }

    override fun toString(): String {
        return "Map(map=$hashMap)"
    }
}