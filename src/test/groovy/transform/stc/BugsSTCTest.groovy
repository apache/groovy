package groovy.transform.stc

/**
 * Unit tests for static type checking : bug fixes.
 *
 * @author Cedric Champeau
 */
class BugsSTCTest extends StaticTypeCheckingTestCase {
    // GROOVY-5456
    void testShouldNotAllowDivOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it / 2 } }
        ''', 'Cannot find matching method java.lang.Object#div(int)'
    }
}
