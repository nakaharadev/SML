package nml.spans

class SpannableText(
    val text: String = "",
    val spans: List<Span> = emptyList(),
) : CharSequence {
    override var length = text.length
        private set

    override fun get(index: Int): Char {
        return text[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return text.subSequence(startIndex, endIndex)
    }

    override fun toString(): String {
        return "SpannableText(\n\ttext=$text\n\tspans=$spans\n)"
    }
}