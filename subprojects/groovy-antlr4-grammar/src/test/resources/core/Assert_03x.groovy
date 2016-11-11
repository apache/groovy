import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

testPostfixExpression()

/***********************************/

void testPostfixExpression() {
    isRendered """
assert x++ == null
       ||  |
       |0  false
       0
        """, {
        def x = 0
        assert x++ == null
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
