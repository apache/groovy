package org.codehaus.groovy.classgen

import groovy.util.GroovyTestCase

/**
 * @author Guillaume Laforge
 */
class CallClosureFieldAsMethodTest extends GroovyTestCase {

    String firstname = "Guillaume"
    def closureMethod = { greeting-> "${greeting} ${firstname}" }
    public static staticClosureMethod = {it}

    /**
     * Check that we can call a closure defined as a field as if it were a normal method
     */
    void testCallToClosureAsMethod() {
        def obj = new CallClosureFieldAsMethodTest()
        assert obj.closureMethod("Hello") == "Hello Guillaume"
    }
    
    void testCallToClosureAsMethodFromStaticField() {
        assert CallClosureFieldAsMethodTest.staticClosureMethod("Hello") == "Hello"
    }

}
