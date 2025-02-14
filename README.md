# SML
Scripted markup language for serializing

### Syntax
```
prefix : SomeObject {
    param:text="Hello, world"
}
```
For this XML will be created object for class SomeClass with constructor like `constructor(text: CharSequence)`.
In this case you should use `CharSequence` for prepare errors in situation where text param contain markdown. About using MD later.
If param is nullable you can set null:
```
prefix : SomeObject {
    param:text=null
}
```

### Use
```kt
// create SMLParser instance
val parser = SMLParser(fileData, SomeObject::class)
// get SMLObject
val SMDObject = parser.parse("prefix")
```

A SMDObject contain nodes list with type `Any?`. For get some object without cast use `<object>.get<Type>(index)`

### using MD
In SML included itself MD language for string type.
Syntax:
```
b&<text>&
```
In this case `b` is indicator for set span type.
All spans:
```
b - bold
i - italian/cursive
m - monospace
u - underline
c - cross out
```
And using in file:
```
prefix : SomeObject {
    param:text="b&Hello&, i&world&"
}
```
Spans you can find in `SpannableText` in field `spans`. `Spannable text` is `CharSequence` what can contain spans with text.
Using:
```kt
// SomeObject.kt
data class SomeObject(
    spannableText: CharSequence
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
