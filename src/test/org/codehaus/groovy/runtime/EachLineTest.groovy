package org.codehaus.groovy.runtime;

class EachLineTest extends GroovyTestCase {
    // discovered and fixed as part of GROOVY-4361
    void testReturnsResultOfApplyingClosureToLastLine() {
        def result = "ONE\nTWO\nTHREE".eachLine { it.toLowerCase() }
        assert result == "three"
    }
}