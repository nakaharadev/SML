package sml

import java.io.File

abstract class CacheOperator(val file: File) {
    abstract fun transform(tokens: List<Token>): Any?
    abstract fun transform(cache: Any?): List<Token>
    abstract fun write(cache: Any?)
    abstract fun read(): Any?
}