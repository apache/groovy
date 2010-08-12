package org.codehaus.groovy.runtime;

class EachLineTest extends GroovyTestCase {
    // GROOVY-4364
    void testReturnsLastValueReturnedByClosure() {
        def result = "ONE\nTWO\nTHREE".eachLine { it.toLowerCase() }
        assert result == "three"
    }
}