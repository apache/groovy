package groovy

class IfWithMethodCallTest extends GroovyTestCase {

    void testIfWithMethodCall() {
        def x = ["foo", "cheese"]

        if ( x.contains("cheese") ) {
            // ignore
        }
        else {
            assert false , "x should contain cheese!"
        }
    }
}
