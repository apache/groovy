package groovy.bugs;

import org.codehaus.groovy.dummy.ClassWithStaticMethod

/**
 * Test case to check if imports can use fully qualified classes for static method calls.
 * Bug reference: Explicit import needed to call static method, GROOVY-935
 */
class StaticMethodImportGroovy935Bug extends GroovyTestCase {
    void testBug() {
        assert ClassWithStaticMethod.staticMethod()
    }
}