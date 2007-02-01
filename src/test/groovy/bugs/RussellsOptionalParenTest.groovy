package groovy.bugs

class RussellsOptionalParenTest extends GroovyTestCase {

    void testMethodCallWithOneParam() {
        def adob = new ArrayList()
        adob.add "hello"
        println adob.get(0)
        println adob.size()
    }
}