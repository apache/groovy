package groovy.bugs

/**
 * @version $Revision$
 */
class UseClosureInClosureBug extends GroovyTestCase {
    void testBugWithPrintln() {
        def inner = { it * 3 }
        def outer1 = { inner(it) + 5 }
        def outer2 = { inner(it + 5) }
        assert 17 == outer1(4)
        assert 27 == outer2(4)
    }
}