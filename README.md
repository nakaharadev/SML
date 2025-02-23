# SML
Scripted markup language for serializing

### Syntax
```sml
prefix : SomeObject {
    param:text="Hello, world"
}
```
For this SML will be created object for class SomeClass with constructor like `constructor(text: CharSequence)`.
In this case you should use `CharSequence` to prevent errors in case the text contains markdown. About using MD later.
If param is nullable you can set null:
```sml
prefix : SomeObject {
    param:text=null
}
```

### Use
```kt
// create SMLParser instance
val parser = SMLParser(fileData, SomeObject::class)
// get SMLObject
val obj = parser.parse("prefix")
```

A SMDObject contain nodes list with type `Any?`. For get some object without cast use `<object>.get<Type>(index)`

### using MD
In SML included itself MD language for string type.
Syntax:
```sml
b&<text>&
```
In this case `b` is indicator for set span type.
All spans:
```sml
b - bold
i - italian/cursive
m - monospace
u - underline
c - cross out
```
And using in file:
```sml
prefix : SomeObject {
    param:text="b&Hello&, i&world&"
}
```
Spans you can find in `SpannableText` in field `spans`. `Spannable text` is `CharSequence` what can contain spans with text.
Using:
```kt
// SomeObject.kt
data class SomeObject(
    val spannableText: CharSequence
)


// Main.kt
// ... get SMLObject
for (node in smlObject.nodes) {
    node as SomeObject
    val spannable = node.spannableText as SpannableText
    val text = spannable.text
    for (span in spannable.spans) {
         // do something with Span
    }
}
```
`Span` object contains start and end index in text and `SpanTypeface` enum object.

### Caching
You can use cache with SML. For this set child of `CacheOperator` in `SMLObject`.
For example with `TextCacheOperator` (have in lib):
```kt
// create SMLParser instance
val parser = SMLParser(fileData, SomeObject::class)
// set cache operator
parser.cacheOperator = TextCacheOperator(File("<path-to-file>"))
// get SMLObject
val obj = parser.parse("prefix")
```
This code create cache file in `<path-to-file>` in text. Parser will read and write cache automatically. If file not exists parser will start tokenize and cache operator create file.

### Custom cache operator
You can create custom cache operator if you want a different cache format.
For example:
```kt
class CustomCacheOperator(file: File) : CacheOperator(file) {
    override fun transform(cache: Any?): List<Token> {
        // here you will convert the read cache into a list of tokens
    }

    override fun transform(tokens: List<Token>): Any? {
        // here you will convert a list of tokens into the cache
    }

    override fun write(cache: Any?) {
        // here you will write a transformed cache
    }

    override fun read(): Any? {
        // here you read saved cache for transform
    }
}
```
Caution! The creation of a new file must be written manually. It should be in `read` method.

### Scripting
Language can use itself scripts without special blocks. For example:
```sml
prefix SomeClass {
    param:obj = obj(cls SomeParamClass, map { text: "Hello, world" })
}
```
In this case used function `obj`. This function have signature `obj(clazz: Cls, params: Map): Any`. `Cls` and `Map` is SML types, but `Any` is Kotlin type because this function creates an object based on the class passed by the first argument.

#### SML types
SML types table:
```sml
╔⁠═⁠═⁠Name═⁠═⁠═⁠╤⁠═⁠═⁠Description═⁠═⁠═⁠╤⁠═⁠ ═⁠Creating═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠╗⁠
║⁠ String  | String value   | "<text>"             ║⁠
+═⁠═⁠═⁠═⁠═⁠═⁠═⁠+----------------+----------------------╢⁠
║⁠ Int     | Integer value  | 0                    ║⁠
+---------+----------------+----------------------╢⁠
║⁠ Float   | Float value    | 0.0 or 0.0f          ║⁠
+---------+----------------+----------------------╢⁠
║⁠ Map     | Hash map       | map {                |
║⁠         |                |     <key>: <value>,  |
║⁠         |                |     ...,             ║⁠
║⁠         |                |     <key>: <value>   |
║⁠         |                | }                    ║⁠
+---------+----------------+----------------------╢⁠
║⁠ Cls     | KClass in      | cls <class name>     |
║⁠         | script         |                      ║⁠
╚⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠╧⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠╧⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠═⁠╝⁠
```
For use Cls the appropriate KClass must be passed