class RussellsOptionalParenTest extends GroovyTestCase {

    void testMethodCallWithOneParam() {
        adob = new ArrayList()
        adob.add "hello"
        println adob.get(0)
        println adob.size()
    }
}