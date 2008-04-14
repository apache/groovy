package groovy.bugs;

import org.codehaus.groovy.dummy.*

/**
 * Test case to check if imports can use wildcard (*) for static method calls.
 * Bug reference: Explicit import needed to call static method, GROOVY-935
 */
class StaticMethodImportBug extends GroovyTestCase {
    void testBug() {
        assert ClassWithStaticMethod.staticMethod()
    }
}