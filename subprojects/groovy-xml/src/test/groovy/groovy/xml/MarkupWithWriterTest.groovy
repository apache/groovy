package groovy.xml

/**
 * This test uses GroovyMarkup with writers other than System.out
 */
class MarkupWithWriterTest extends TestXmlSupport {

    void testSmallTreeWithStringWriter() {
        def writer = new java.io.StringWriter()
        def b = new MarkupBuilder(writer)

        b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
        println writer.toString()
//        assertEquals "<root1 a='5' b='7'>\n" +
//                "  <elem1>hello1</elem1>\n" +
//                "  <elem2>hello2</elem2>\n" +
//                "  <elem3 x='7' />\n" +
//                "</root1>", writer.toString()
    }

    void testWriterUseInScriptFile() {
        assertScriptFile 'src/test/groovy/xml/UseMarkupWithWriterScript.groovy'
    }
}