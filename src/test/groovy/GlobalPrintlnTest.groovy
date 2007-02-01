package groovy

class GlobalPrintlnTest extends GroovyTestCase {

    void testGlobalPrintln() {
        println("Hello World!")
	}

    void testGlobalPrint() {
        print("Hello ")
        println("World!")
    }
}