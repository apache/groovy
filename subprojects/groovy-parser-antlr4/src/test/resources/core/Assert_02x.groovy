import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

testBinaryExpression()

/***********************************/
void testBinaryExpression() {
    isRendered """
assert a * b
       | | |
       0 0 1
        """, {
        def a = 0
        def b = 1
        assert a * b
    }

    isRendered """
assert a[b]
       |||
       ||0
       |false
       [false]
        """, {
        def a = [false]
        def b = 0
        assert a[b]
    }
}

static isRendered(String expectedRendering, Closure failingAssertion) {
    try {
        failingAssertion.call();
        assert false, "assertion should have failed but didn't"
    } catch (PowerAssertionError e) {
        assert expectedRendering.trim() == e.message.trim()
    }
}
