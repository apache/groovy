package groovy

class GlobalPrintlnTest extends GroovyTestCase {

    void testGlobalPrintln() {
        println("Hello World!")
	}

    void testGlobalPrint() {
        print("Hello ")
        println("World!")
    }
    
    void testWriterTest() {
        def sw = new StringWriter()
        sw.print("foo")
        assert sw.toString() == 'foo'
    }
}