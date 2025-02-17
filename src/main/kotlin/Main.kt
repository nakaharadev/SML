import com.ndev.sml.SMLObject
import com.ndev.sml.SMLParser
import sml.TextCacheOperator
import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    val sml = """
        SomeClass {
            param:text = "Hello, world"
        }
    """.trimIndent()

    var mills = measureTimeMillis {
        val parser = SMLParser(sml, SomeClass::class)
        parser.cacheOperator = null
        parser.parse(null)
    }
    println("Without cache: $mills")

    mills = measureTimeMillis {
        val parser = SMLParser(sml, SomeClass::class)
        parser.cacheOperator = TextCacheOperator(File("C:\\Users\\user\\Downloads\\cache.txt"))
        parser.parse(null)
    }
    println("With cache: $mills")
}