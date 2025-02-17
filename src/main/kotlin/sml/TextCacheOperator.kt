package sml

import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class TextCacheOperator(file: File) : CacheOperator(file) {
    override fun transform(tokens: List<Token>): Any {
        var result = ""
        for (token in tokens) {
            result += "${token.toStringSimplify()}\n"
        }

        return result
    }

    override fun transform(cache: Any?): List<Token> {
        if (cache == null)
            return emptyList()

        cache as String

        val result = ArrayList<Token>()

        val cacheLines = cache.split('\n')
        for (line in cacheLines) {
            if (line.isEmpty())
                continue

            val split = line.split(';')
            if (split.size == 1)
                result.add(Token((TokenType of split[0]) ?: continue))
            else {
                result.add(
                    Token(
                        (TokenType of split[0]) ?: continue,
                        split[1]
                    )
                )
            }
        }

        return result
    }

    override fun write(cache: Any?) {
        if (cache == null)
            return

        if (!file.exists())
            return

        cache as String

        val stream = file.outputStream()
        val output = OutputStreamWriter(stream)
        output.write(cache)
        output.flush()
        output.close()
    }

    override fun read(): Any? {
        if (!file.exists()) {
            file.createNewFile()
            return null
        }

        val stream = file.inputStream()
        val input = InputStreamReader(stream)
        val text = input.readText()
        input.close()

        if (text.isEmpty())
            return null

        return text
    }
}