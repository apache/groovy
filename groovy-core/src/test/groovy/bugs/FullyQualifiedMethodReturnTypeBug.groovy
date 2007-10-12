package groovy.bugs

/**
 * @version $Revision$
 */
class FullyQualifiedMethodReturnTypeBug extends GroovyTestCase {

    void testBug() {
        def s = foo()
        assert s.length() == 3
    }

    java.lang.String foo() {
        return "hey"
    }

}