class StaticPrintlnTest extends GroovyTestCase {

    void testStaticPrint() {
        main([null].toArray())
	}
	
    static void main(args) {
        println("called with: " + args)
    }
}