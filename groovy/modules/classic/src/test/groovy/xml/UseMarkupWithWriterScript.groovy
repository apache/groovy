// used by MarkupWithWriterTest.testWriterUseInScriptFile

writer = new java.io.StringWriter()
b = new groovy.xml.MarkupBuilder(writer)

b.root1(a:5)
println writer.toString()
assert "<root1 a='5' />" == writer.toString()