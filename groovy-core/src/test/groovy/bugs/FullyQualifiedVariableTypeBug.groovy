package groovy.bugs

/**
 * @version $Revision$
 */
class FullyQualifiedVariableTypeBug extends GroovyTestCase {

    void testBug() {
        java.lang.String s = "hey"
        assert s.length() == 3
    }

}