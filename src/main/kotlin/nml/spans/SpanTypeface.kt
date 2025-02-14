package nml.spans

enum class SpanTypeface(val indicator: Char) {
    BOLD('b'),
    CURSIVE('i'),
    MONOSPACE('m'),
    UNDERLINE('u'),
    CROSS_OUT('c');

    companion object {
        infix fun of(indicator: Char): SpanTypeface? {
            return entries.find { it.indicator == indicator }
        }
    }
}